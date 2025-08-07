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
        private const val MAX_RETRY_COUNT = 5
        private const val SUBSCRIPTION_RETRY_DELAY = 3000L // 구독 재시도 지연
    }

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    // ⭐ 개선: 여러 구독 요청을 큐로 관리
    private val pendingSubscriptions = mutableSetOf<Long>()
    private val activeSubscriptions = mutableSetOf<Long>() // 활성 구독 추적
    private var retryCount = 0

    // 메시지 수신을 위한 Flow - replay를 1로 설정하여 마지막 메시지를 보장
    private val _messageFlow = MutableSharedFlow<ChatMessageDTO>(
        replay = 1,
        extraBufferCapacity = 50
    )
    val messageFlow: SharedFlow<ChatMessageDTO> = _messageFlow.asSharedFlow()

    // 읽음 알림용 Flow
    private val _readFlow = MutableSharedFlow<ChatReadDTO>(
        replay = 1,
        extraBufferCapacity = 20
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
        Log.d(TAG, "🔌 연결 요청 - 현재 상태: ${_detailedConnectionStatus.value}")

        // 이미 연결 중이거나 연결됨
        if (_detailedConnectionStatus.value in listOf(ConnectionState.CONNECTING, ConnectionState.CONNECTED)) {
            Log.d(TAG, "이미 연결 중이거나 연결된 상태")
            return
        }

        Log.d(TAG, "🔌 WebSocket 연결 시작...")
        _detailedConnectionStatus.value = ConnectionState.CONNECTING

        try {
            // 기존 연결 정리
            cleanupConnection()

            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_URL).apply {
                // 하트비트 설정
                withClientHeartbeat(10000) // 10초
                withServerHeartbeat(10000) // 10초
            }

            // 연결 상태 관찰
            val lifecycleDisposable = stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { lifecycleEvent -> handleConnectionEvent(lifecycleEvent) },
                    { error ->
                        Log.e(TAG, "❌ Lifecycle 관찰 에러", error)
                        handleConnectionFailure(error)
                    }
                )

            compositeDisposable.add(lifecycleDisposable)

            // 연결 시작
            stompClient!!.connect()

            // 연결 타임아웃 처리
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(CONNECTION_TIMEOUT)
                if (_detailedConnectionStatus.value == ConnectionState.CONNECTING) {
                    Log.e(TAG, "❌ 연결 타임아웃")
                    handleConnectionTimeout()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 연결 시작 중 예외", e)
            handleConnectionFailure(e)
        }
    }

    private fun cleanupConnection() {
        try {
            compositeDisposable.clear()
            stompClient?.disconnect()
            stompClient = null
            activeSubscriptions.clear()
        } catch (e: Exception) {
            Log.w(TAG, "연결 정리 중 경고", e)
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
                activeSubscriptions.clear()

                // 예상치 못한 연결 종료 시 재연결
                if (retryCount < MAX_RETRY_COUNT) {
                    attemptReconnection()
                }
            }

            LifecycleEvent.Type.ERROR -> {
                Log.e(TAG, "❌ WebSocket 에러: ${lifecycleEvent.exception}")
                handleConnectionFailure(lifecycleEvent.exception)
            }

            else -> {
                Log.d(TAG, "기타 상태: ${lifecycleEvent.type}")
            }
        }
    }

    private fun handleConnectionFailure(error: Throwable?) {
        _connectionStatus.value = false
        _detailedConnectionStatus.value = ConnectionState.FAILED
        activeSubscriptions.clear()

        Log.e(TAG, "연결 실패: ${error?.message}")

        if (retryCount < MAX_RETRY_COUNT) {
            attemptReconnection()
        } else {
            Log.e(TAG, "최대 재시도 횟수 초과 - 연결 포기")
        }
    }

    private fun attemptReconnection() {
        retryCount++
        val delay = (retryCount * 2000L).coerceAtMost(10000L) // 최대 10초

        Log.w(TAG, "🔄 재연결 시도 $retryCount/$MAX_RETRY_COUNT (${delay}ms 후)")
        _detailedConnectionStatus.value = ConnectionState.RECONNECTING

        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(delay)
            connect()
        }
    }

    private fun handleConnectionTimeout() {
        Log.e(TAG, "❌ 연결 타임아웃 발생")
        handleConnectionFailure(Exception("Connection timeout"))
    }

    fun subscribeToChatRoom(roomId: Long) {
        Log.d(TAG, "📡 subscribeToChatRoom 호출됨 - roomId: $roomId")

        // 이미 구독 중인 경우 중복 방지
        if (activeSubscriptions.contains(roomId)) {
            Log.d(TAG, "이미 구독 중인 채팅방: $roomId")
            return
        }

        stompClient?.let { client ->
            if (!client.isConnected) {
                Log.w(TAG, "⚠️ 연결되지 않음. 구독 예약: $roomId")
                pendingSubscriptions.add(roomId)

                // 연결이 안 되어 있으면 연결 시작
                if (_detailedConnectionStatus.value == ConnectionState.DISCONNECTED) {
                    connect()
                }
                return
            }

            performSubscription(roomId)
        } ?: run {
            Log.e(TAG, "❌ stompClient가 null - 구독 예약 후 연결 시작")
            pendingSubscriptions.add(roomId)
            connect()
        }
    }

    private fun processPendingSubscriptions() {
        if (pendingSubscriptions.isEmpty()) {
            Log.d(TAG, "대기 중인 구독이 없음")
            return
        }

        Log.d(TAG, "⏳ 대기 중인 구독 처리: ${pendingSubscriptions.size}개")

        val subscriptionsToProcess = pendingSubscriptions.toSet()
        pendingSubscriptions.clear()

        subscriptionsToProcess.forEach { roomId ->
            Log.d(TAG, "🔄 대기 구독 실행: roomId=$roomId")
            kotlinx.coroutines.GlobalScope.launch {
                // 약간의 지연을 두어 연결 안정화
                kotlinx.coroutines.delay(500L)
                performSubscription(roomId)
            }
        }
    }

    private fun performSubscription(roomId: Long) {
        Log.d(TAG, "✅ 실제 구독 수행 시작 - roomId: $roomId")

        val client = stompClient ?: run {
            Log.e(TAG, "❌ stompClient가 null - 구독 실패")
            pendingSubscriptions.add(roomId)
            return
        }

        if (!client.isConnected) {
            Log.w(TAG, "⚠️ 구독 시점에 연결이 끊어짐. 재예약")
            pendingSubscriptions.add(roomId)
            return
        }

        try {
            // 메시지 구독
            val messageTopicDisposable = client.topic("/topic/chat.room.$roomId")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { stompMessage ->
                        handleReceivedMessage(stompMessage, roomId)
                    },
                    { throwable ->
                        Log.e(TAG, "❌ 채팅방 구독 에러 - roomId: $roomId", throwable)
                        activeSubscriptions.remove(roomId)
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

            compositeDisposable.add(messageTopicDisposable)
            compositeDisposable.add(readTopicDisposable)

            // 활성 구독에 추가
            activeSubscriptions.add(roomId)
            Log.d(TAG, "✅ 구독 완료: roomId=$roomId, 총 활성 구독: ${activeSubscriptions.size}개")

        } catch (e: Exception) {
            Log.e(TAG, "❌ 구독 중 예외 발생 - roomId: $roomId", e)
            retrySubscription(roomId)
        }
    }

    private fun retrySubscription(roomId: Long) {
        Log.w(TAG, "🔄 구독 재시도 예약: roomId=$roomId")
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(SUBSCRIPTION_RETRY_DELAY)
            if (_connectionStatus.value) {
                performSubscription(roomId)
            } else {
                pendingSubscriptions.add(roomId)
            }
        }
    }

    private fun handleReceivedMessage(stompMessage: StompMessage, roomId: Long) {
        try {
            Log.d(TAG, "📨 원시 메시지 수신: roomId=$roomId, payload=${stompMessage.payload}")

            val chatMessage = gson.fromJson(stompMessage.payload, ChatMessageDTO::class.java)
            Log.d(TAG, "📨 메시지 파싱 성공: ${chatMessage.message} (room: $roomId)")

            // 메시지 Flow에 전달 - 강제 emit 사용
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    _messageFlow.emit(chatMessage)
                    Log.d(TAG, "✅ 메시지 Flow 전달 성공")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 메시지 Flow 전달 실패", e)
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

            kotlinx.coroutines.GlobalScope.launch {
                try {
                    _readFlow.emit(readDto)
                    Log.d(TAG, "✅ 읽음 알림 Flow 전달 성공")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 읽음 알림 Flow 전달 실패", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 읽음 알림 파싱 에러", e)
        }
    }

    fun sendMessage(messageDTO: ChatMessageDTO) {
        val client = stompClient
        if (client == null || !client.isConnected) {
            Log.e(TAG, "❌ 연결되지 않은 상태에서 메시지 전송 시도")
            return
        }

        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 전송 중 예외", e)
        }
    }

    fun markAsRead(readDTO: ChatReadDTO) {
        val client = stompClient
        if (client == null || !client.isConnected) {
            Log.w(TAG, "⚠️ 연결되지 않은 상태에서 읽음 처리 시도")
            return
        }

        try {
            val json = gson.toJson(readDTO)
            Log.d(TAG, "📖 읽음 처리 요청: roomId=${readDTO.chatRoomId}, userId=${readDTO.userId}, lastReadCid=${readDTO.lastReadCid}")

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
        } catch (e: Exception) {
            Log.e(TAG, "❌ 읽음 처리 중 예외", e)
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 WebSocket 연결 해제")
        try {
            compositeDisposable.clear()
            stompClient?.disconnect()
            stompClient = null
            pendingSubscriptions.clear()
            activeSubscriptions.clear()
            _connectionStatus.value = false
            _detailedConnectionStatus.value = ConnectionState.DISCONNECTED
            retryCount = 0
        } catch (e: Exception) {
            Log.e(TAG, "❌ 연결 해제 중 오류", e)
        }
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

    // 구독 상태 확인 메서드 추가
    fun getActiveSubscriptions(): Set<Long> = activeSubscriptions.toSet()

    fun isSubscribedToRoom(roomId: Long): Boolean = activeSubscriptions.contains(roomId)
}