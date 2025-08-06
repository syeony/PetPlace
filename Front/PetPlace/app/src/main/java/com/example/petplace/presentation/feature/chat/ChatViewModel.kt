package com.example.petplace.presentation.feature.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
    private val chatRepository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val webSocketManager = WebSocketManager()

    // 현재 사용자 ID와 채팅방 ID
    private val currentUserId = 6L
    private val currentChatRoomId: Long = savedStateHandle["chatRoomId"] ?: 0L

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _showAttachmentOptions = MutableStateFlow(false)
    val showAttachmentOptions: StateFlow<Boolean> = _showAttachmentOptions.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private var lastMessageId = 0L

    init {
        Log.d(TAG, "ChatViewModel 초기화 - 사용자: $currentUserId, 채팅방: $currentChatRoomId")

        // 초기 메시지 로드
        loadInitialMessages()

        // WebSocket 설정
        setupWebSocket()
    }

    private fun setupWebSocket() {
        // 연결 상태 관찰
        viewModelScope.launch {
            webSocketManager.connectionStatus.collect { isConnected ->
                Log.d(TAG, "연결 상태 변경: $isConnected")
                _connectionStatus.value = isConnected

                if (isConnected) {
                    // 연결되면 자동으로 구독됨 (WebSocketManager에서 처리)
                    markMessagesAsRead()
                }
            }
        }

        // 메시지 수신 관찰 - UI에 즉시 반영
        viewModelScope.launch {
            webSocketManager.messageFlow.collect { messageDto ->
                Log.d(TAG, "📨 웹소켓 메시지 수신: '${messageDto.message}' (chatId: ${messageDto.chatId})")

                // 메시지를 ChatMessage로 변환
                val newMessage = messageDto.toChatMessage(currentUserId)
                Log.d(TAG, "📨 변환 완료: isFromMe=${newMessage.isFromMe}")

                // UI 스레드에서 상태 업데이트 보장
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _messages.value = _messages.value + newMessage
                    Log.d(TAG, "💬 UI 업데이트 완료: 총 ${_messages.value.size}개 메시지")
                }

                // 최신 메시지 ID 업데이트 (읽음 처리용)
                messageDto.chatId?.let { chatId ->
                    lastMessageId = chatId
                    Log.d(TAG, "🔄 최신 메시지 ID 업데이트: $lastMessageId")
                }
            }
        }

        // 읽음 알림 수신 관찰 - 읽음 상태 UI 반영
        viewModelScope.launch {
            webSocketManager.readFlow.collect { readDto ->
                Log.d(TAG, "📖 읽음 알림 수신: userId=${readDto.userId}, lastReadCid=${readDto.lastReadCid}")

                // UI 스레드에서 읽음 상태 업데이트
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _messages.value = _messages.value.map { message ->
                        if (message.id != null && message.id <= readDto.lastReadCid) {
                            message.copy(isRead = true)
                        } else {
                            message
                        }
                    }
                    Log.d(TAG, "✅ 읽음 상태 UI 반영 완료")
                }
            }
        }

        // WebSocket 연결 시작 및 구독
        webSocketManager.connect()
        webSocketManager.subscribeToChatRoom(currentChatRoomId)
    }

    // ChatMessageDTO -> ChatMessage 변환
    private fun ChatMessageDTO.toChatMessage(myUserId: Long): ChatMessage {
        Log.d(TAG, "🔄 메시지 변환: dto.userId=${this.userId}, myUserId=$myUserId")
        return ChatMessage(
            id = this.chatId,
            content = this.message,
            isFromMe = this.userId == myUserId,
            timestamp = this.createdAt ?: getCurrentTimestamp(),
            isRead = false
        ).also {
            Log.d(TAG, "🔄 변환 결과: isFromMe=${it.isFromMe}")
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
        val message = messageInput.value.trim()
        if (message.isBlank()) return

        if (!_connectionStatus.value) {
            Log.w(TAG, "연결되지 않은 상태에서 메시지 전송 시도")
            addSystemMessage("연결되지 않았습니다. 연결을 확인해주세요.")
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

            Log.d(TAG, "📤 메시지 전송 완료: $message")
            // 주의: 내가 보낸 메시지도 웹소켓을 통해 수신되므로 여기서 UI에 추가하지 않음
            // messageFlow에서 수신할 때 UI에 반영됨

        } catch (e: Exception) {
            Log.e(TAG, "메시지 전송 실패", e)
            addSystemMessage("메시지 전송 실패: ${e.message}")
        }
    }

    fun markMessagesAsRead() {
        if (lastMessageId > 0) {
            val readDTO = ChatReadDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                lastReadCid = lastMessageId
            )
            webSocketManager.markAsRead(readDTO)
            Log.d(TAG, "읽음 처리 요청: lastMessageId=$lastMessageId")
        }
    }

    private fun loadInitialMessages() {
        viewModelScope.launch {
            try {
                val result = chatRepository.getChatMessages(currentChatRoomId)
                result.onSuccess { messageDTOs ->
                    val chatMessages = messageDTOs.map { dto ->
                        ChatMessage(
                            id = dto.chatId,
                            content = dto.message,
                            isFromMe = dto.userId == currentUserId,
                            timestamp = dto.createdAt ?: getCurrentTimestamp(),
                            isRead = false
                        )
                    }
                    _messages.value = chatMessages

                    if (messageDTOs.isNotEmpty()) {
                        lastMessageId = messageDTOs.last().chatId ?: 0L
                    }

                    Log.d(TAG, "초기 메시지 로드 완료: ${chatMessages.size}개")
                }.onFailure { e ->
                    Log.e(TAG, "메시지 로드 실패", e)
                    addSystemMessage("대화 기록을 불러오는데 실패했습니다.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "메시지 로드 중 예외", e)
                addSystemMessage("대화 기록 로드 중 오류가 발생했습니다.")
            }
        }
    }

    private fun addSystemMessage(content: String) {
        val systemMessage = ChatMessage(
            id = null,
            content = content,
            isFromMe = false,
            timestamp = getCurrentTimestamp(),
            isRead = false
        )
        // UI 스레드에서 직접 업데이트
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            _messages.value = _messages.value + systemMessage
            Log.d(TAG, "🔔 시스템 메시지 추가: '$content', 총 메시지: ${_messages.value.size}개")
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
            Log.d(TAG, "ViewModel 정리 완료")
        } catch (e: Exception) {
            Log.e(TAG, "ViewModel 정리 중 오류", e)
        }
    }
}