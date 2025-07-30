package com.example.petplace.presentation.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.chat.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _showAttachmentOptions = MutableStateFlow(false)
    val showAttachmentOptions: StateFlow<Boolean> = _showAttachmentOptions.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        loadMessages()
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
            val newMessage = ChatMessage(messageInput.value, true)
            _messages.value = _messages.value + newMessage
            _messageInput.value = ""
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _messages.value = listOf(
                ChatMessage("아직 판매중이신가요?", false),
                ChatMessage("네, 아직 판매중입니다!", true),
                ChatMessage("직거래 가능한가요? 위치가 어디신가요?", false),
                ChatMessage("강남역 근처에서 직거래 가능해요!", true),
                ChatMessage("좋네요!", false),
                ChatMessage("내일 오후에 만날 수 있을까요?", false)
            )
        }
    }
}