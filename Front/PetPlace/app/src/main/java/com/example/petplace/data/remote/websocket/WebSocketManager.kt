package com.example.petplace.data.remote.websocket

import android.util.Log
import com.example.petplace.data.model.chat.ChatMessageDTO
import com.example.petplace.data.model.chat.ChatReadDTO
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val SERVER_URL = "ws://43.201.108.195:8081/ws/chat/websocket"
        private const val CONNECTION_TIMEOUT = 10000L // 10초
        private const val MAX_RETRY_COUNT = 3
    }

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    // ⭐ 개선: 여러 구독 요청을 큐로 관리
    private val pendingSubscriptions = mutableSetOf<Long>()
    private var retryCount = 0

    // 메시지 수신을 위한 Flow - replay를 1로 설정하여 마지막 메시지를 보장
    private val _messageFlow = MutableSharedFlow<ChatMessageDTO>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val messageFlow: SharedFlow<ChatMessageDTO> = _messageFlow.asSharedFlow()

    // 읽음 알림용 Flow
    private val _readFlow = MutableSharedFlow<ChatReadDTO>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val readFlow: SharedFlow<ChatReadDTO> = _readFlow.asSharedFlow()

    // 연결 상태를 위한 Flow
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private val _detailedConnectionStatus = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val detailedConnectionStatus: StateFlow<ConnectionState> = _detailedConnectionStatus.asStateFlow()

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        FAILED
    }

    fun connect() {
        if (stompClient != null && _detailedConnectionStatus.value == ConnectionState.CONNECTING) {
            Log.d(TAG, "이미 연결 중입니다")
            return
        }

        if (stompClient?.isConnected == true) {
            Log.d(TAG, "이미 연결되어 있습니다")
            return
        }

        Log.d(TAG, "🔌 WebSocket 연결 시작...")
        _detailedConnectionStatus.value = ConnectionState.CONNECTING
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_URL)

        // 연결 상태 관찰
        val lifecycleDisposable = stompClient!!.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                handleConnectionEvent(lifecycleEvent)
//                when (lifecycleEvent.type) {
//                    LifecycleEvent.Type.OPENED -> {
//                        Log.d(TAG, "WebSocket 연결됨")
//                        _connectionStatus.value = true
//
//                        // 연결 완료 후 대기 중인 구독 처리
//                        pendingRoomId?.let { roomId ->
//                            Log.d(TAG, "연결 완료 후 대기 중인 구독 실행: roomId=$roomId")
//                            performSubscription(roomId)
//                            pendingRoomId = null
//                        }
//                    }
//                    LifecycleEvent.Type.CLOSED -> {
//                        Log.d(TAG, "WebSocket 연결 종료됨")
//                        _connectionStatus.value = false
//                    }
//                    LifecycleEvent.Type.ERROR -> {
//                        Log.e(TAG, "WebSocket 에러: ${lifecycleEvent.exception}")
//                        _connectionStatus.value = false
//                    }
//                    else -> {
//                        Log.d(TAG, "기타 상태: ${lifecycleEvent.type}")
//                    }
//                }
            }

        compositeDisposable.add(lifecycleDisposable)

        // 연결 시작
        stompClient!!.connect()

        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(CONNECTION_TIMEOUT)
            if (_detailedConnectionStatus.value == ConnectionState.CONNECTING) {
                Log.e(TAG, "❌ 연결 타임아웃")
                handleConnectionTimeout()
            }
        }
    }

    private fun handleConnectionEvent(lifecycleEvent: LifecycleEvent) {
        when (lifecycleEvent.type) {
            LifecycleEvent.Type.OPENED -> {
                Log.d(TAG, "✅ WebSocket 연결 성공!")
                _connectionStatus.value = true
                _detailedConnectionStatus.value = ConnectionState.CONNECTED
                retryCount = 0

                // ⭐ 개선: 모든 대기 중인 구독 처리
                processPendingSubscriptions()
            }

            LifecycleEvent.Type.CLOSED -> {
                Log.d(TAG, "🔌 WebSocket 연결 종료됨")
                _connectionStatus.value = false
                _detailedConnectionStatus.value = ConnectionState.DISCONNECTED

                // ⭐ 자동 재연결 시도
                if (retryCount < MAX_RETRY_COUNT) {
                    attemptReconnection()
                }
            }

            LifecycleEvent.Type.ERROR -> {
                Log.e(TAG, "❌ WebSocket 에러: ${lifecycleEvent.exception}")
                _connectionStatus.value = false
                _detailedConnectionStatus.value = ConnectionState.FAILED

                // ⭐ 에러 시에도 재연결 시도
                if (retryCount < MAX_RETRY_COUNT) {
                    attemptReconnection()
                }
            }

            else -> {
                Log.d(TAG, "기타 상태: ${lifecycleEvent.type}")
            }
        }
    }

    private fun attemptReconnection() {
        retryCount++
        val delay = (retryCount * 2000L).coerceAtMost(10000L) // 최대 10초

        Log.w(TAG, "🔄 재연결 시도 $retryCount/$MAX_RETRY_COUNT (${delay}ms 후)")
        _detailedConnectionStatus.value = ConnectionState.RECONNECTING

        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(delay)
            disconnect()
            kotlinx.coroutines.delay(1000L) // 잠깐 대기
            connect()
        }
    }

    private fun handleConnectionTimeout() {
        Log.e(TAG, "❌ 연결 타임아웃 발생")
        _detailedConnectionStatus.value = ConnectionState.FAILED
        disconnect()

        if (retryCount < MAX_RETRY_COUNT) {
            attemptReconnection()
        }
    }

    fun subscribeToChatRoom(roomId: Long) {
        Log.d(TAG, "📡 subscribeToChatRoom 호출됨 - roomId: $roomId")

        stompClient?.let { client ->
            if (!client.isConnected) {
                Log.w(TAG, "⚠️ stompClient가 아직 연결되지 않음. 연결 후 구독 예약")
                pendingSubscriptions.add(roomId)  // ⭐ Set으로 중복 방지

                // ⭐ 연결이 안 되어 있으면 연결 시작
                if (_detailedConnectionStatus.value == ConnectionState.DISCONNECTED) {
                    connect()
                }
                return
            }

            performSubscription(roomId)
        } ?: run {
            Log.e(TAG, "❌ stompClient가 null입니다. 연결을 먼저 시작합니다.")
            pendingSubscriptions.add(roomId)
            connect()
        }
    }

    private fun processPendingSubscriptions() {
        Log.d(TAG, "⏳ 대기 중인 구독 처리: ${pendingSubscriptions.size}개")

        val subscriptionsToProcess = pendingSubscriptions.toSet() // 복사본 생성
        pendingSubscriptions.clear()

        subscriptionsToProcess.forEach { roomId ->
            Log.d(TAG, "🔄 대기 구독 실행: roomId=$roomId")
            performSubscription(roomId)
        }
    }

    private fun performSubscription(roomId: Long) {
        Log.d(TAG, "✅ 실제 구독 수행 - roomId: $roomId")

        val client = stompClient ?: run {
            Log.e(TAG, "❌ stompClient가 null - 구독 실패")
            return
        }

        if (!client.isConnected) {
            Log.w(TAG, "⚠️ 구독 시점에 연결이 끊어짐. 재예약")
            pendingSubscriptions.add(roomId)
            return
        }

        try {
            // 메시지 구독
            val topicDisposable = client.topic("/topic/chat.room.$roomId")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { stompMessage ->
                        handleReceivedMessage(stompMessage, roomId)
                    },
                    { throwable ->
                        Log.e(TAG, "❌ 채팅방 구독 에러 - roomId: $roomId", throwable)
                        // ⭐ 구독 실패 시 재시도
                        retrySubscription(roomId)
                    }
                )

            // 읽음 알림 구독
            val readTopicDisposable = client.topic("/topic/chat.room.$roomId.read")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { stompMessage ->
                        handleReceivedReadNotification(stompMessage, roomId)
                    },
                    { throwable ->
                        Log.e(TAG, "❌ 읽음 알림 구독 에러 - roomId: $roomId", throwable)
                    }
                )

            compositeDisposable.add(topicDisposable)
            compositeDisposable.add(readTopicDisposable)

            Log.d(TAG, "✅ 구독 완료: roomId=$roomId")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 구독 중 예외 발생 - roomId: $roomId", e)
            retrySubscription(roomId)
        }

    }

    private fun retrySubscription(roomId: Long) {
        Log.w(TAG, "🔄 구독 재시도 예약: roomId=$roomId")
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(2000L) // 2초 후 재시도
            if (_connectionStatus.value) {
                performSubscription(roomId)
            } else {
                pendingSubscriptions.add(roomId)
            }
        }
    }

    private fun handleReceivedMessage(stompMessage: StompMessage, roomId: Long) {
        try {
            val chatMessage = gson.fromJson(stompMessage.payload, ChatMessageDTO::class.java)
            Log.d(TAG, "📨 메시지 수신 및 UI 전달: ${chatMessage.message} (room: $roomId)")

            val success = _messageFlow.tryEmit(chatMessage)
            Log.d(TAG, "💬 메시지 Flow 전달 ${if (success) "성공" else "실패"}")

            if (!success) {
                Log.w(TAG, "⚠️ 메시지 Flow 버퍼가 가득참. 강제 전달 시도")
                kotlinx.coroutines.GlobalScope.launch {
                    _messageFlow.emit(chatMessage)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 파싱 에러", e)
        }
    }

    private fun handleReceivedReadNotification(stompMessage: StompMessage, roomId: Long) {
        try {
            val readDto = gson.fromJson(stompMessage.payload, ChatReadDTO::class.java)
            Log.d(TAG, "📖 읽음 알림 수신: $readDto (room: $roomId)")

            val success = _readFlow.tryEmit(readDto)
            Log.d(TAG, "✅ 읽음 알림 Flow 전달 ${if (success) "성공" else "실패"}")

            if (!success) {
                kotlinx.coroutines.GlobalScope.launch {
                    _readFlow.emit(readDto)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 읽음 알림 파싱 에러", e)
        }
    }

    fun sendMessage(messageDTO: ChatMessageDTO) {
        stompClient?.let { client ->
            val json = gson.toJson(messageDTO)
            Log.d(TAG, "📤 메시지 전송 시도: ${messageDTO.message}")

            val sendDisposable = client.send("/app/chat.sendMessage", json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "✅ 메시지 전송 완료")
                    },
                    { throwable ->
                        Log.e(TAG, "❌ 메시지 전송 에러", throwable)
                    }
                )

            compositeDisposable.add(sendDisposable)
        } ?: Log.e(TAG, "❌ stompClient가 null - 메시지 전송 불가")
    }

    fun markAsRead(readDTO: ChatReadDTO) {
        stompClient?.let { client ->
            val json = gson.toJson(readDTO)
            val readDisposable = client.send("/app/chat.updateRead", json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "✅ 읽음 처리 완료")
                    },
                    { throwable ->
                        Log.e(TAG, "❌ 읽음 처리 에러", throwable)
                    }
                )

            compositeDisposable.add(readDisposable)
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 WebSocket 연결 해제")
        compositeDisposable.clear()
        stompClient?.disconnect()
        stompClient = null
        pendingSubscriptions.clear()
        _connectionStatus.value = false
        _detailedConnectionStatus.value = ConnectionState.DISCONNECTED
        retryCount = 0
    }

    fun isConnected(): Boolean {
        return stompClient?.isConnected ?: false
    }

    fun forceReconnect() {
        Log.d(TAG, "🔄 강제 재연결 시도")
        disconnect()
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(1000L)
            connect()
        }
    }
}