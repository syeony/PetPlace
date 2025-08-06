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
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class WebSocketManager {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val SERVER_URL = "ws://43.201.108.195:8081/ws/chat/websocket"
    }

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    // 구독 대기 중인 roomId를 저장
    private var pendingRoomId: Long? = null

    // 메시지 수신을 위한 Flow
    private val _messageFlow = MutableSharedFlow<ChatMessageDTO>()
    val messageFlow: SharedFlow<ChatMessageDTO> = _messageFlow.asSharedFlow()

    // 읽음 알림용 Flow
    private val _readFlow = MutableSharedFlow<ChatReadDTO>()
    val readFlow: SharedFlow<ChatReadDTO> = _readFlow.asSharedFlow()

    // 연결 상태를 위한 Flow
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    fun connect() {
        if (stompClient != null) {
            Log.d(TAG, "Already connecting or connected")
            return
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_URL)

        // 연결 상태 관찰
        val lifecycleDisposable = stompClient!!.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d(TAG, "WebSocket 연결됨")
                        _connectionStatus.tryEmit(true)

                        // 연결 완료 후 대기 중인 구독 처리
                        pendingRoomId?.let { roomId ->
                            Log.d(TAG, "연결 완료 후 대기 중인 구독 실행: roomId=$roomId")
                            performSubscription(roomId)
                            pendingRoomId = null
                        }
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.d(TAG, "WebSocket 연결 종료됨")
                        _connectionStatus.tryEmit(false)
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "WebSocket 에러: ${lifecycleEvent.exception}")
                        _connectionStatus.tryEmit(false)
                    }
                    else -> {
                        Log.d(TAG, "기타 상태: ${lifecycleEvent.type}")
                    }
                }
            }

        compositeDisposable.add(lifecycleDisposable)

        // 연결 시작
        stompClient!!.connect()
    }

    fun subscribeToChatRoom(roomId: Long) {
        Log.d(TAG, "📡 subscribeToChatRoom 호출됨 - roomId: $roomId")

        stompClient?.let { client ->
            if (!client.isConnected) {
                Log.w(TAG, "⚠️ stompClient가 아직 연결되지 않음. 연결 후 구독 예약")
                pendingRoomId = roomId
                return
            }

            performSubscription(roomId)
        } ?: run {
            Log.e(TAG, "stompClient가 null입니다")
        }
    }

    private fun performSubscription(roomId: Long) {
        Log.d(TAG, "✅ 실제 구독 수행 - roomId: $roomId")

        val client = stompClient ?: return

        // 메시지 구독
        val topicDisposable = client.topic("/topic/chat.room.$roomId")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { stompMessage ->
                    try {
                        val chatMessage = gson.fromJson(stompMessage.payload, ChatMessageDTO::class.java)
                        Log.d(TAG, "메시지 수신: ${chatMessage.message}")
                        _messageFlow.tryEmit(chatMessage)
                    } catch (e: Exception) {
                        Log.e(TAG, "메시지 파싱 에러", e)
                    }
                },
                { throwable ->
                    Log.e(TAG, "채팅방 구독 에러", throwable)
                }
            )

        // 읽음 알림 구독
        val readTopicDisposable = client.topic("/topic/chat.room.$roomId.read")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { stompMessage ->
                    try {
                        val readDto = gson.fromJson(stompMessage.payload, ChatReadDTO::class.java)
                        Log.d(TAG, "읽음 알림 수신: $readDto")
                        _readFlow.tryEmit(readDto)
                    } catch (e: Exception) {
                        Log.e(TAG, "읽음 알림 파싱 에러", e)
                    }
                },
                { throwable ->
                    Log.e(TAG, "읽음 알림 구독 에러", throwable)
                }
            )

        compositeDisposable.add(topicDisposable)
        compositeDisposable.add(readTopicDisposable)

        Log.d(TAG, "구독 완료: roomId=$roomId")
    }

    fun sendMessage(messageDTO: ChatMessageDTO) {
        stompClient?.let { client ->
            val json = gson.toJson(messageDTO)
            val sendDisposable = client.send("/app/chat.sendMessage", json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "메시지 전송 완료")
                    },
                    { throwable ->
                        Log.e(TAG, "메시지 전송 에러", throwable)
                    }
                )

            compositeDisposable.add(sendDisposable)
        }
    }

    fun markAsRead(readDTO: ChatReadDTO) {
        stompClient?.let { client ->
            val json = gson.toJson(readDTO)
            val readDisposable = client.send("/app/chat.updateRead", json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "읽음 처리 완료")
                    },
                    { throwable ->
                        Log.e(TAG, "읽음 처리 에러", throwable)
                    }
                )

            compositeDisposable.add(readDisposable)
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
        stompClient = null
        pendingRoomId = null
        Log.d(TAG, "WebSocket 연결 해제")
    }

    fun isConnected(): Boolean {
        return stompClient?.isConnected ?: false
    }
}