package com.example.petplace.presentation.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.chat.ChatMessage
import com.example.petplace.data.model.chat.ChatMessageDTO
import com.example.petplace.data.model.chat.ChatReadDTO
import com.example.petplace.data.remote.websocket.WebSocketManager
import com.example.petplace.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val webSocketManager = WebSocketManager()

    // 현재 사용자 ID와 채팅방 ID (예시, 실제론 DI로)
    private val currentUserId = 6L
    private val currentChatRoomId = 1L

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _showAttachmentOptions = MutableStateFlow(false)
    val showAttachmentOptions: StateFlow<Boolean> = _showAttachmentOptions.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private var lastMessageId = 0L
    private var isSubscribed = false

    init {
        Log.d(TAG, "ChatViewModel 초기화 시작")
        Log.d(TAG, "현재 사용자 ID: $currentUserId, 채팅방 ID: $currentChatRoomId")
        setupWebSocketConnection()
        loadInitialMessages()
        // 메시지 수신
        viewModelScope.launch {
            webSocketManager.messageFlow.collect { messageDto ->
                Log.d(TAG, "수신한 메시지 DTO: $messageDto")
                val message = messageDto.toChatMessage(currentUserId)
                _messages.update { old -> old + message }
                messageDto.chatId?.let {
                    lastMessageId = it
                    Log.d(TAG, "📩 최신 메시지 ID 업데이트: $lastMessageId")
                }
            }
        }
        // 읽음 알림 수신
        viewModelScope.launch {
            webSocketManager.readFlow.collect { readDto ->
                Log.d(TAG, "읽음 알림 수신: $readDto")
                // 읽음 표시 반영
                _messages.update { list ->
                    list.map { msg ->
                        if (msg.id != null && msg.id <= readDto.lastReadCid) {
                            msg.copy(isRead = true) // ChatMessage에 isRead가 있다고 가정!
                        } else msg
                    }
                }
            }
        }
    }

    // ChatMessageDTO -> ChatMessage 변환 (id와 isRead 필드 추가 가정)
    fun ChatMessageDTO.toChatMessage(myUserId: Long): ChatMessage {
        return ChatMessage(
            id = this.chatId,   // ChatMessage data class에 id: Long? 추가
            content = this.message,
            isFromMe = this.userId == myUserId,
            timestamp = this.createdAt ?: "",
            isRead = false      // 받은 시점에선 읽음처리 안된 상태로 추가
        )
    }

    private fun setupWebSocketConnection() {
        Log.d(TAG, "WebSocket 연결 설정 시작")
        Log.d(TAG, "WebSocketManager 인스턴스: $webSocketManager")

        viewModelScope.launch {
            Log.d(TAG, "연결 상태 관찰 시작")
            webSocketManager.connectionStatus.collect { isConnected ->
                Log.d(TAG, "🔔 연결 상태 변경 수신: $isConnected")
                _connectionStatus.value = isConnected
                if (isConnected && !isSubscribed) {
                    Log.d(TAG, "연결 완료! 채팅방 구독 시도: roomId=$currentChatRoomId")
                    try {
                        webSocketManager.subscribeToChatRoom(currentChatRoomId)
                        isSubscribed = true
                        Log.d(TAG, "채팅방 구독 요청 완료")
                        markMessagesAsRead() // ✅ 입장하자마자 읽음 처리
                        val subscribeMessage = ChatMessage(
                            id = null, // 안내 메시지는 id 없음
                            content = "채팅방에 연결되었습니다. (방 ID: $currentChatRoomId)",
                            isFromMe = false,
                            timestamp = getCurrentTimestamp(),
                            isRead = false
                        )
                        _messages.value = _messages.value + subscribeMessage

                    } catch (e: Exception) {
                        Log.e(TAG, "채팅방 구독 중 오류 발생", e)
                        val errorMessage = ChatMessage(
                            id = null,
                            content = "채팅방 구독 실패: ${e.message}",
                            isFromMe = false,
                            timestamp = getCurrentTimestamp(),
                            isRead = false
                        )
                        _messages.value = _messages.value + errorMessage
                    }
                } else if (!isConnected) {
                    Log.w(TAG, "연결이 끊어짐")
                    isSubscribed = false
                    val disconnectMessage = ChatMessage(
                        id = null,
                        content = "연결이 끊어졌습니다. 재연결을 시도합니다...",
                        isFromMe = false,
                        timestamp = getCurrentTimestamp(),
                        isRead = false
                    )
                    _messages.value = _messages.value + disconnectMessage
                }
            }
        }

        viewModelScope.launch {
            Log.d(TAG, "메시지 수신 관찰 시작")
            webSocketManager.messageFlow.collect { messageDTO ->
                Log.d(TAG, "메시지 수신: $messageDTO")
                val chatMessage = messageDTO.toChatMessage(currentUserId)
                messageDTO.chatId?.let {
                    lastMessageId = it
                    Log.d(TAG, "마지막 메시지 ID 업데이트: $lastMessageId")
                }
                _messages.value = _messages.value + chatMessage
                Log.d(TAG, "메시지 리스트에 추가 완료. 현재 메시지 수: ${_messages.value.size}")
            }
        }

        // WebSocket 연결 시작
        Log.d(TAG, "WebSocket 연결 시작 요청")
        try {
            webSocketManager.connect()
            Log.d(TAG, "WebSocket connect() 호출 완료")
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                Log.d(TAG, "🧪 3초 후 수동 연결 상태 확인")
                Log.d(TAG, "🧪 현재 WebSocketManager 연결 상태: ${webSocketManager.connectionStatus}")
                Log.d(TAG, "🧪 현재 ChatViewModel 연결 상태: ${_connectionStatus.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket 연결 중 오류 발생", e)
            val errorMessage = ChatMessage(
                id = null,
                content = "연결 오류: ${e.message}",
                isFromMe = false,
                timestamp = getCurrentTimestamp(),
                isRead = false
            )
            _messages.value = _messages.value + errorMessage
        }
    }

    fun onMessageInputChange(newValue: String) {
        _messageInput.value = newValue
    }

    fun toggleAttachmentOptions() {
        val newValue = !_showAttachmentOptions.value
        _showAttachmentOptions.value = newValue
        if (_showAttachmentOptions.value) {
            _messageInput.value = ""
        }
    }

    fun closeAttachmentOptions() {
        _showAttachmentOptions.value = false
    }

    fun sendMessage() {
        val message = messageInput.value
        if (message.isNotBlank()) {
            if (!_connectionStatus.value) {
                val warningMessage = ChatMessage(
                    id = null,
                    content = "연결되지 않았습니다. 연결을 확인해주세요.",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp(),
                    isRead = false
                )
                _messages.value = _messages.value + warningMessage
                return
            }

            val messageDTO = ChatMessageDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                message = message,
                imageUrls = emptyList()
            )

            try {
                webSocketManager.sendMessage(messageDTO)
                _messageInput.value = ""
                val myMessage = ChatMessage(
                    id = null, // 전송 직후엔 서버 응답 전이므로 id 미지정
                    content = message,
                    isFromMe = true,
                    timestamp = getCurrentTimestamp(),
                    isRead = false
                )
                _messages.value = _messages.value + myMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = null,
                    content = "메시지 전송 실패: ${e.message}",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp(),
                    isRead = false
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }

    fun markMessagesAsRead() {
        if (lastMessageId > 0) {
            val readDTO = ChatReadDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                lastReadCid = lastMessageId
            )
            try {
                webSocketManager.markAsRead(readDTO)
            } catch (e: Exception) {
                Log.e(TAG, "읽음 처리 중 오류 발생", e)
            }
        }
    }

    private fun loadInitialMessages() {
        viewModelScope.launch {
            _messages.value = listOf(
                ChatMessage(
                    id = null,
                    content = "대화 기록을 불러오는 중...",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp(),
                    isRead = false
                )
            )
            val result = chatRepository.getChatMessages(currentChatRoomId)
            result.onSuccess {
                val chatMessages = it.map {
                    ChatMessage(
                        id = it.chatId,
                        content = it.message,
                        isFromMe = it.userId == currentUserId,
                        timestamp = it.createdAt ?: getCurrentTimestamp(),
                        isRead = false
                    )
                }
                _messages.value = chatMessages
                if (it.isNotEmpty()) {
                    lastMessageId = it.last().chatId ?: 0L
                }
            }.onFailure {
                _messages.value = listOf(
                    ChatMessage(
                        id = null,
                        content = "대화 기록을 불러오는데 실패했습니다.",
                        isFromMe = false,
                        timestamp = getCurrentTimestamp(),
                        isRead = false
                    )
                )
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            webSocketManager.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket 연결 해제 중 오류", e)
        }
    }
}