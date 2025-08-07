package com.example.petplace.presentation.feature.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
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
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Locale
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
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    private val currentUserId = userInfo?.userId ?: 0
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
    private var shouldMarkAsRead = true

    init {
        Log.d(TAG, "🚀 ChatViewModel 초기화 - 사용자: $currentUserId, 채팅방: $currentChatRoomId")

        // 초기 메시지 로드
        loadInitialMessages()

        // WebSocket 설정
        setupWebSocket()
    }

    private fun setupWebSocket() {
        Log.d(TAG, "🔌 WebSocket 설정 시작")

        // 연결 상태 관찰
        viewModelScope.launch {
            webSocketManager.connectionStatus.collect { isConnected ->
                Log.d(TAG, "📡 연결 상태 변경: $isConnected")
                _connectionStatus.value = isConnected

                if (isConnected) {
                    // 연결되면 자동으로 구독됨 (WebSocketManager에서 처리)
                    markMessagesAsRead()
                }
            }
        }

        // 메시지 수신 관찰 - 메인 스레드에서 직접 처리
        viewModelScope.launch(Dispatchers.Main) {
            webSocketManager.messageFlow.collect { messageDto ->
                Log.d(TAG, "📨 웹소켓 메시지 수신 처리 시작: '${messageDto.message}' (chatId: ${messageDto.chatId})")

                try {
                    // 메시지를 ChatMessage로 변환
                    val newMessage = messageDto.toChatMessage(currentUserId)
                    Log.d(TAG, "🔄 메시지 변환 완료: isFromMe=${newMessage.isFromMe}")

                    // 메시지 리스트에 추가 (이미 메인 스레드이므로 직접 업데이트)
                    val updatedMessages = _messages.value.toMutableList().apply {
                        add(newMessage)
                    }
                    _messages.value = updatedMessages

                    Log.d(TAG, "✅ UI 업데이트 완료: 총 ${_messages.value.size}개 메시지")

                    // 최신 메시지 ID 업데이트 (읽음 처리용)
                    messageDto.chatId?.let { chatId ->
                        if (messageDto.userId != currentUserId) {
                            // 상대방 메시지를 받았을 때만 읽음 처리
                            Log.d(TAG, "📖 상대방 메시지 수신 - 읽음 처리 예약: chatId=$chatId")
                            lastMessageId = maxOf(lastMessageId, chatId)

                            // ⭐ 지연 후 읽음 처리 (UI 업데이트 완료 후)
                            kotlinx.coroutines.delay(500L)
                            markMessagesAsRead()
                            Log.d(TAG, "🔄 상대방 메시지 읽음 처리: $lastMessageId")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ 메시지 처리 중 오류", e)
                }
            }
        }

        // 읽음 알림 수신 관찰 - 읽음 상태 UI 반영
        viewModelScope.launch(Dispatchers.Main) {
            webSocketManager.readFlow.collect { readDto ->
                Log.d(TAG, "📖 읽음 알림 수신: userId=${readDto.userId}, lastReadCid=${readDto.lastReadCid}")

                try {
                    if (readDto.userId != currentUserId) {
                        val updatedMessages = _messages.value.map { message ->
                            // 내가 보낸 메시지 중에서 읽음 처리된 ID 이하인 것들만 읽음 처리
                            if (message.isFromMe &&
                                message.id != null &&
                                message.id <= readDto.lastReadCid) {
                                message.copy(isRead = true)
                            } else {
                                message
                            }
                        }
                        _messages.value = updatedMessages
                        Log.d(TAG, "✅ 상대방이 내 메시지 읽음 처리 완료")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ 읽음 상태 처리 중 오류", e)
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
        val isFromMe = this.userId == myUserId
        return ChatMessage(
            id = this.chatId,
            content = this.message,
            isFromMe = this.userId == myUserId,
            timestamp = formatToHHmm(this.createdAt!!),
            isRead = !isFromMe
        ).also {
            Log.d(TAG, "🔄 변환 결과: content='${it.content}', isFromMe=${it.isFromMe}")
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
        if (message.isBlank()) {
            Log.w(TAG, "⚠️ 빈 메시지 전송 시도")
            return
        }

        if (!_connectionStatus.value) {
            Log.w(TAG, "⚠️ 연결되지 않은 상태에서 메시지 전송 시도")
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

        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 전송 실패", e)
            addSystemMessage("메시지 전송 실패: ${e.message}")
        }
    }

    fun markMessagesAsRead() {
        if (!shouldMarkAsRead) {
            Log.d(TAG, "📖 읽음 처리가 비활성화됨")
            return
        }

        if (!_connectionStatus.value) {
            Log.w(TAG, "⚠️ 연결되지 않은 상태 - 읽음 처리 연기")
            return
        }

        val latestOpponentMessageId = _messages.value
            .filter { !it.isFromMe && it.id != null && it.id > 0 }
            .maxByOrNull { it.id!! }
            ?.id

        Log.d(TAG, "📊 읽음 처리 대상 조사: latestOpponentMessageId=$latestOpponentMessageId, lastMessageId=$lastMessageId")

        val targetMessageId = when {
            latestOpponentMessageId != null -> latestOpponentMessageId
            lastMessageId > 0 -> lastMessageId
            else -> {
                Log.d(TAG, "📖 읽음 처리할 메시지가 없음")
                return
            }
        }

        if (targetMessageId > 0) {
            Log.d(TAG, "📖 읽음 처리 실행: targetMessageId=$targetMessageId")

            val readDTO = ChatReadDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                lastReadCid = targetMessageId
            )

            try {
                webSocketManager.markAsRead(readDTO)
                Log.d(TAG, "✅ 읽음 처리 요청 전송 완료")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 읽음 처리 요청 실패", e)
            }
        } else {
            Log.d(TAG, "📖 유효하지 않은 메시지 ID: $targetMessageId")
        }
    }

    // ⭐ 화면이 활성화될 때 호출되는 메서드 (Compose에서 사용)
    fun onScreenVisible() {
        Log.d(TAG, "👀 화면이 보임 - 읽음 처리 활성화")
        shouldMarkAsRead = true
        markMessagesAsRead()
    }

    // ⭐ 화면이 비활성화될 때 호출되는 메서드
    fun onScreenHidden() {
        Log.d(TAG, "🙈 화면이 숨겨짐 - 읽음 처리 비활성화")
        shouldMarkAsRead = false
    }

    private fun loadInitialMessages() {
        Log.d(TAG, "📥 초기 메시지 로드 시작")

        viewModelScope.launch {
            try {
                val result = chatRepository.getChatMessages(currentChatRoomId)
                result.onSuccess { messageDTOs ->
                    val chatMessages = messageDTOs.map { dto ->
                        val isFromMe = dto.userId == currentUserId
                        ChatMessage(
                            id = dto.chatId,
                            content = dto.message,
                            isFromMe = isFromMe,
                            timestamp = formatToHHmm(dto.createdAt),
                            // ⭐ 초기 로드 시 읽음 상태 결정 로직 개선
                            // 실제로는 서버에서 읽음 상태 정보를 받아와야 하지만,
                            // 임시로 내가 보낸 메시지는 읽음으로, 상대방 메시지도 읽음으로 처리
                            isRead = true // 이미 저장된 메시지들은 모두 읽음 처리
                        )
                    }

                    // 메인 스레드에서 UI 업데이트
                    launch(Dispatchers.Main) {
                        _messages.value = chatMessages
                    }

                    if (messageDTOs.isNotEmpty()) {
                        lastMessageId = messageDTOs.last().chatId ?: 0L
                    }

                    // ⭐ 초기 메시지 로드 후 읽음 처리
                    val latestMessageId = messageDTOs
                        .filter { it.userId != currentUserId } // 상대방 메시지만
                        .maxByOrNull { it.chatId ?: 0L }
                        ?.chatId

                    if (latestMessageId != null && latestMessageId > 0) {
                        lastMessageId = latestMessageId
                        Log.d(TAG, "📝 초기 로드 완료 - 최신 상대방 메시지 ID: $lastMessageId")

                        // ⭐ 초기 로드 완료 후 읽음 처리 (연결 완료를 기다림)
                        kotlinx.coroutines.delay(2000L)
                        markMessagesAsRead()
                    }

                    Log.d(TAG, "✅ 초기 메시지 로드 완료: ${chatMessages.size}개")
                }.onFailure { e ->
                    Log.e(TAG, "❌ 메시지 로드 실패", e)
                    addSystemMessage("대화 기록을 불러오는데 실패했습니다.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 메시지 로드 중 예외", e)
                addSystemMessage("대화 기록 로드 중 오류가 발생했습니다.")
            }
        }
    }

    private fun formatToHHmm(utcDateTime: String): String {
        // 1. UTC 기준 입력 파싱
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // 입력은 UTC 기준

        // 2. KST 기준으로 출력 포맷 정의
        val outputFormat = java.text.SimpleDateFormat("a hh:mm", java.util.Locale("ko", "KR"))
        outputFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul") // 출력은 KST

        // 3. 파싱 및 포맷
        val date = inputFormat.parse(utcDateTime)
        return outputFormat.format(date!!)
    }


    private fun addSystemMessage(content: String) {
        val systemMessage = ChatMessage(
            id = null,
            content = content,
            isFromMe = false,
            timestamp = getCurrentTimestamp(),
            isRead = false
        )

        // 메인 스레드에서 직접 업데이트
        viewModelScope.launch(Dispatchers.Main) {
            val updatedMessages = _messages.value.toMutableList().apply {
                add(systemMessage)
            }
            _messages.value = updatedMessages
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
            shouldMarkAsRead = false
            webSocketManager.disconnect()
            Log.d(TAG, "🧹 ViewModel 정리 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ViewModel 정리 중 오류", e)
        }
    }
}