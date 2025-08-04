package com.example.petplace.presentation.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.chat.ChatMessage
import com.example.petplace.data.model.chat.ChatMessageDTO
import com.example.petplace.data.model.chat.ChatReadDTO
import com.example.petplace.data.remote.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val webSocketManager = WebSocketManager()

    // 현재 사용자 ID와 채팅방 ID (실제로는 의존성 주입이나 다른 방법으로 설정)
    private val currentUserId = 1L
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

    init {
        setupWebSocketConnection()
        loadInitialMessages()
    }

    private fun setupWebSocketConnection() {
        viewModelScope.launch {
            // 연결 상태 관찰
            webSocketManager.connectionStatus.collect { isConnected ->
                _connectionStatus.value = isConnected
                if (isConnected) {
                    // 연결되면 채팅방 구독
                    webSocketManager.subscribeToChatRoom(currentChatRoomId)
                }
            }
        }

        viewModelScope.launch {
            // 메시지 수신 관찰
            webSocketManager.messageFlow.collect { messageDTO ->
                val chatMessage = ChatMessage(
                    content = messageDTO.message,
                    isFromMe = messageDTO.userId == currentUserId,
                    timestamp = messageDTO.createdAt ?: ""
                )

                // 메시지 ID 업데이트
                messageDTO.chatId?.let {
                    lastMessageId = it
                }

                _messages.value = _messages.value + chatMessage
            }
        }

        // WebSocket 연결 시작
        webSocketManager.connect()
    }

    fun onMessageInputChange(newValue: String) {
        _messageInput.value = newValue
    }

    fun toggleAttachmentOptions() {
        _showAttachmentOptions.value = !_showAttachmentOptions.value
        if (_showAttachmentOptions.value) {
            _messageInput.value = ""
        }
    }

    fun closeAttachmentOptions() {
        _showAttachmentOptions.value = false
    }

    fun sendMessage() {
        if (messageInput.value.isNotBlank()) {
            val messageDTO = ChatMessageDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                message = messageInput.value,
                imageUrls = emptyList()
            )

            webSocketManager.sendMessage(messageDTO)
            _messageInput.value = ""
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
        }
    }

    private fun loadInitialMessages() {
        // 기존 더미 데이터는 제거하고 서버에서 기존 메시지를 로드하는 로직으로 대체 가능
        viewModelScope.launch {
            _messages.value = listOf(
                ChatMessage("서버 연결을 시도하고 있습니다...", false, "")
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}