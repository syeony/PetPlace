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

    // 현재 사용자 ID와 채팅방 ID (실제로는 의존성 주입이나 다른 방법으로 설정)
    private val currentUserId = 5L
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
    }

    private fun setupWebSocketConnection() {
        Log.d(TAG, "WebSocket 연결 설정 시작")
        Log.d(TAG, "WebSocketManager 인스턴스: $webSocketManager")

        viewModelScope.launch {
            Log.d(TAG, "연결 상태 관찰 시작")
            Log.d(TAG, "connectionStatus Flow 구독 시작")
            // 연결 상태 관찰
            webSocketManager.connectionStatus.collect { isConnected ->
                Log.d(TAG, "🔔 연결 상태 변경 수신: $isConnected")
                Log.d(TAG, "🔔 이전 상태: ${_connectionStatus.value}, 새 상태: $isConnected")
                _connectionStatus.value = isConnected

                if (isConnected && !isSubscribed) {
                    Log.d(TAG, "연결 완료! 채팅방 구독 시도: roomId=$currentChatRoomId")
                    // 연결되면 채팅방 구독
                    try {
                        webSocketManager.subscribeToChatRoom(currentChatRoomId)
                        isSubscribed = true
                        Log.d(TAG, "채팅방 구독 요청 완료")

                        // 구독 완료 메시지 추가
                        val subscribeMessage = ChatMessage(
                            content = "채팅방에 연결되었습니다. (방 ID: $currentChatRoomId)",
                            isFromMe = false,
                            timestamp = getCurrentTimestamp()
                        )
                        _messages.value = _messages.value + subscribeMessage

                    } catch (e: Exception) {
                        Log.e(TAG, "채팅방 구독 중 오류 발생", e)
                        val errorMessage = ChatMessage(
                            content = "채팅방 구독 실패: ${e.message}",
                            isFromMe = false,
                            timestamp = getCurrentTimestamp()
                        )
                        _messages.value = _messages.value + errorMessage
                    }
                } else if (!isConnected) {
                    Log.w(TAG, "연결이 끊어짐")
                    isSubscribed = false
                    val disconnectMessage = ChatMessage(
                        content = "연결이 끊어졌습니다. 재연결을 시도합니다...",
                        isFromMe = false,
                        timestamp = getCurrentTimestamp()
                    )
                    _messages.value = _messages.value + disconnectMessage
                }
            }
        }

        viewModelScope.launch {
            Log.d(TAG, "메시지 수신 관찰 시작")
            // 메시지 수신 관찰
            webSocketManager.messageFlow.collect { messageDTO ->
                Log.d(TAG, "메시지 수신: $messageDTO")
                Log.d(TAG, "수신된 메시지 - 사용자ID: ${messageDTO.userId}, 내용: ${messageDTO.message}")

                val chatMessage = ChatMessage(
                    content = messageDTO.message,
                    isFromMe = messageDTO.userId == currentUserId,
                    timestamp = messageDTO.createdAt ?: getCurrentTimestamp()
                )

                // 메시지 ID 업데이트
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

            // 3초 후 수동으로 연결 상태 확인
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                Log.d(TAG, "🧪 3초 후 수동 연결 상태 확인")
                Log.d(TAG, "🧪 현재 WebSocketManager 연결 상태: ${webSocketManager.connectionStatus}")
                Log.d(TAG, "🧪 현재 ChatViewModel 연결 상태: ${_connectionStatus.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket 연결 중 오류 발생", e)
            val errorMessage = ChatMessage(
                content = "연결 오류: ${e.message}",
                isFromMe = false,
                timestamp = getCurrentTimestamp()
            )
            _messages.value = _messages.value + errorMessage
        }
    }

    fun onMessageInputChange(newValue: String) {
        Log.v(TAG, "메시지 입력 변경: '$newValue'")
        _messageInput.value = newValue
    }

    fun toggleAttachmentOptions() {
        val newValue = !_showAttachmentOptions.value
        Log.d(TAG, "첨부파일 옵션 토글: $newValue")
        _showAttachmentOptions.value = newValue
        if (_showAttachmentOptions.value) {
            _messageInput.value = ""
            Log.d(TAG, "첨부파일 옵션 열림 - 메시지 입력 초기화")
        }
    }

    fun closeAttachmentOptions() {
        Log.d(TAG, "첨부파일 옵션 닫기")
        _showAttachmentOptions.value = false
    }

    fun sendMessage() {
        val message = messageInput.value
        Log.d(TAG, "메시지 전송 시도: '$message'")

        if (message.isNotBlank()) {
            if (!_connectionStatus.value) {
                Log.w(TAG, "연결되지 않음 - 메시지 전송 불가")
                val warningMessage = ChatMessage(
                    content = "연결되지 않았습니다. 연결을 확인해주세요.",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp()
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

            Log.d(TAG, "메시지 DTO 생성: $messageDTO")

            try {
                webSocketManager.sendMessage(messageDTO)
                Log.d(TAG, "메시지 전송 완료")
                _messageInput.value = ""
                Log.d(TAG, "입력 필드 초기화")

                // 즉시 내 메시지를 화면에 표시 (서버 응답 대기하지 않음)
                val myMessage = ChatMessage(
                    content = message,
                    isFromMe = true,
                    timestamp = getCurrentTimestamp()
                )
                _messages.value = _messages.value + myMessage
                Log.d(TAG, "내 메시지 즉시 표시 완료")

            } catch (e: Exception) {
                Log.e(TAG, "메시지 전송 중 오류 발생", e)
                val errorMessage = ChatMessage(
                    content = "메시지 전송 실패: ${e.message}",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp()
                )
                _messages.value = _messages.value + errorMessage
            }
        } else {
            Log.w(TAG, "빈 메시지 전송 시도 - 무시됨")
        }
    }

    fun markMessagesAsRead() {
        Log.d(TAG, "메시지 읽음 처리 시도 - lastMessageId: $lastMessageId")

        if (lastMessageId > 0) {
            val readDTO = ChatReadDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                lastReadCid = lastMessageId
            )

            Log.d(TAG, "읽음 DTO 생성: $readDTO")

            try {
                webSocketManager.markAsRead(readDTO)
                Log.d(TAG, "읽음 처리 요청 완료")
            } catch (e: Exception) {
                Log.e(TAG, "읽음 처리 중 오류 발생", e)
            }
        } else {
            Log.w(TAG, "읽음 처리할 메시지가 없음 (lastMessageId = 0)")
        }
    }

    private fun loadInitialMessages() {
        Log.d(TAG, "초기 메시지 로드 시작")
        viewModelScope.launch {
            _messages.value = listOf(ChatMessage(
                content = "대화 기록을 불러오는 중...",
                isFromMe = false,
                timestamp = getCurrentTimestamp()
            ))

            val result = chatRepository.getChatMessages(currentChatRoomId)

            result.onSuccess {
                val chatMessages = it.map {
                    ChatMessage(
                        content = it.message,
                        isFromMe = it.userId == currentUserId,
                        timestamp = it.createdAt ?: getCurrentTimestamp()
                    )
                }
                _messages.value = chatMessages
                Log.d(TAG, "초기 메시지 로드 성공: ${it.size}개")
            }.onFailure {
                Log.e(TAG, "초기 메시지 로드 실패", it)
                _messages.value = listOf(ChatMessage(
                    content = "대화 기록을 불러오는데 실패했습니다.",
                    isFromMe = false,
                    timestamp = getCurrentTimestamp()
                ))
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    override fun onCleared() {
        Log.d(TAG, "ChatViewModel 정리 시작")
        super.onCleared()
        try {
            webSocketManager.disconnect()
            Log.d(TAG, "WebSocket 연결 해제 완료")
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket 연결 해제 중 오류", e)
        }
        Log.d(TAG, "ChatViewModel 정리 완료")
    }
}