package com.example.petplace.presentation.feature.chat

import android.net.Uri
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
import com.example.petplace.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val imageRepository: ImageRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val READ_MARK_DELAY = 1000L // 읽음 처리 지연
        private const val CONNECTION_RETRY_INTERVAL = 5000L // 연결 재시도 간격
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

    private val _chatPartnerId = MutableStateFlow<Long?>(null)
    val chatPartnerId: StateFlow<Long?> = _chatPartnerId.asStateFlow()

    private val _chatPartnerName = MutableStateFlow<String?>(null)
    val chatPartnerName: StateFlow<String?> = _chatPartnerName.asStateFlow()

    private val _chatPartnerProfileImage = MutableStateFlow<String?>(null)
    val chatPartnerProfileImage: StateFlow<String?> = _chatPartnerProfileImage.asStateFlow()

    private val _imageUploadStatus = MutableStateFlow<ImageUploadStatus>(ImageUploadStatus.Idle)
    val imageUploadStatus: StateFlow<ImageUploadStatus> = _imageUploadStatus.asStateFlow()


    // 화면 가시성 상태 관리
    private var isScreenVisible = false
    private var lastReadMessageId = 0L
    private var readMarkJob: Job? = null
    private var connectionMonitorJob: Job? = null

    init {
        Log.d(TAG, "🚀 ChatViewModel 초기화 - 사용자: $currentUserId, 채팅방: $currentChatRoomId")

        if (currentChatRoomId > 0) {
            initializeChat()
        } else {
            Log.e(TAG, "❌ 유효하지 않은 채팅방 ID: $currentChatRoomId")
        }
    }

    private fun initializeChat() {
        Log.d(TAG, "🔧 채팅 초기화 시작")

        // 1. 채팅 상대방 정보 로드
        loadChatPartnerInfo()

        // 2. WebSocket 설정
        setupWebSocket()

        // 3. 초기 메시지 로드 (WebSocket 연결과 병렬 실행)
        loadInitialMessages()

        // 4. 연결 모니터링 시작
        startConnectionMonitoring()
    }

    private fun startConnectionMonitoring() {
        connectionMonitorJob?.cancel()
        connectionMonitorJob = viewModelScope.launch {
            while (true) {
                delay(CONNECTION_RETRY_INTERVAL)

                if (!webSocketManager.isConnected()) {
                    Log.w(TAG, "🔍 연결 끊김 감지 - 재연결 시도")
                    ensureConnection()
                } else if (!webSocketManager.isSubscribedToRoom(currentChatRoomId)) {
                    Log.w(TAG, "🔍 구독 끊김 감지 - 재구독 시도")
                    webSocketManager.subscribeToChatRoom(currentChatRoomId)
                }
            }
        }
    }

    private fun ensureConnection() {
        if (!webSocketManager.isConnected()) {
            Log.d(TAG, "🔌 연결 확인 및 재연결")
            webSocketManager.forceReconnect()

            // 재연결 후 구독 보장
            viewModelScope.launch {
                delay(2000L) // 연결 안정화 대기
                if (webSocketManager.isConnected()) {
                    webSocketManager.subscribeToChatRoom(currentChatRoomId)
                }
            }
        }
    }

    // 채팅 상대방 정보를 로드하는 함수
    private fun loadChatPartnerInfo() {
        Log.d(TAG, "👤 채팅 상대방 정보 로드 시작")

        viewModelScope.launch {
            try {
                // 채팅방 참가자 목록을 가져와서 상대방 정보 찾기
                val participants = chatRepository.getParticipants(currentChatRoomId).getOrThrow()
                Log.d(TAG, "참가자 목록: $participants")

                // 현재 사용자가 아닌 참가자 찾기 (상대방)
                val partner = participants.firstOrNull { it.userId != currentUserId }

                if (partner != null) {
                    _chatPartnerId.value = partner.userId
                    _chatPartnerName.value = partner.nickname
                    // 프로필 이미지 URL 처리
                    val profileImageUrl = partner.profileImageUrl?.let { imageUrl ->
                        if (imageUrl.startsWith("http")) {
                            imageUrl
                        } else {
                            "http://43.201.108.195:8081$imageUrl" // 실제 서버 URL
                        }
                    }
                    _chatPartnerProfileImage.value = profileImageUrl
                    Log.d(TAG, "✅ 채팅 상대방 정보 로드 완료: ${partner.nickname}")
                } else {
                    Log.w(TAG, "⚠️ 채팅 상대방을 찾을 수 없음")
                    _chatPartnerName.value = "알 수 없는 사용자"
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 채팅 상대방 정보 로드 실패", e)
                _chatPartnerName.value = "사용자"
            }
        }
    }

    private fun setupWebSocket() {
        Log.d(TAG, "🔌 WebSocket 설정 시작")

        // 연결 상태 관찰
        viewModelScope.launch {
            webSocketManager.connectionStatus.collect { isConnected ->
                Log.d(TAG, "📡 연결 상태 변경: $isConnected")
                _connectionStatus.value = isConnected

                if (isConnected) {
                    Log.d(TAG, "✅ 연결됨 - 구독 및 읽음 처리 시작")
                    // 연결 성공 시 구독 보장
                    webSocketManager.subscribeToChatRoom(currentChatRoomId)

                    // 화면이 보이는 상태라면 읽음 처리
                    if (isScreenVisible) {
                        scheduleReadMarkUpdate()
                    }
                }
            }
        }

        // 메시지 수신 관찰 - 메인 스레드에서 직접 처리
        viewModelScope.launch(Dispatchers.Main) {
            webSocketManager.messageFlow.collect { messageDto ->
                Log.d(TAG, "📨 웹소켓 메시지 수신 처리 시작: '${messageDto.message}' (chatId: ${messageDto.chatId})")

                try {
                    val newMessage = messageDto.toChatMessage(currentUserId)

                    // 중복 메시지 체크
                    val isDuplicate = _messages.value.any { existingMessage ->
                        existingMessage.id == newMessage.id &&
                                existingMessage.content == newMessage.content &&
                                existingMessage.isFromMe == newMessage.isFromMe
                    }

                    if (!isDuplicate) {
                        addMessageToUI(newMessage)

                        // 상대방 메시지 수신 시 읽음 처리
                        if (!newMessage.isFromMe && newMessage.id != null) {
                            lastReadMessageId = maxOf(lastReadMessageId, newMessage.id)
                            if (isScreenVisible) {
                                scheduleReadMarkUpdate()
                            }
                        }
                    } else {
                        Log.d(TAG, "중복 메시지 무시: ${newMessage.content}")
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

                if (readDto.userId != currentUserId) {
                    updateMessagesReadStatus(readDto.lastReadCid)
                }
            }
        }

        // WebSocket 연결 시작 및 구독
        webSocketManager.connect()
        webSocketManager.subscribeToChatRoom(currentChatRoomId)
    }

    private fun addMessageToUI(newMessage: ChatMessage) {
        val updatedMessages = _messages.value.toMutableList().apply {
            add(newMessage)
        }
        _messages.value = updatedMessages
        Log.d(TAG, "✅ UI 메시지 추가: 총 ${_messages.value.size}개")
    }

    private fun updateMessagesReadStatus(lastReadCid: Long) {
        val updatedMessages = _messages.value.map { message ->
            if (message.isFromMe && message.id != null && message.id <= lastReadCid) {
                message.copy(isRead = true)
            } else {
                message
            }
        }
        _messages.value = updatedMessages
        Log.d(TAG, "✅ 읽음 상태 업데이트 완료")
    }

    // ChatMessageDTO -> ChatMessage 변환
    private fun ChatMessageDTO.toChatMessage(myUserId: Long): ChatMessage {
        Log.d(TAG, "🔄 메시지 변환: dto.userId=${this.userId}, myUserId=$myUserId")
        Log.d(TAG, "🔄 메시지 변환 시작: dto.message='${this.message}'")
        val isFromMe = this.userId == myUserId
        // 메시지 타입 구분
        val (messageType, displayContent, imageUrls) = parseMessage(this.message)
        val result = ChatMessage(
            id = this.chatId,
            content = displayContent,
            messageType = messageType,
            imageUrls = imageUrls,
            isFromMe = isFromMe,
            timestamp = formatToHHmm(this.createdAt!!),
            isRead = !isFromMe
        )

        Log.d(TAG, "🔄 변환 결과: type=${result.messageType}, imageUrls=${result.imageUrls}, content='${result.content}'")
        return result
    }

    // 메시지 파싱 함수
    private fun parseMessage(message: String): Triple<MessageType, String, List<String>> {
        Log.d(TAG, "🔍 메시지 파싱 시작: '$message'")

        return when {
            message.startsWith("IMAGE:") -> {
                val urlPart = message.removePrefix("IMAGE:")
                Log.d(TAG, "🔍 IMAGE 헤더 감지, URL 부분: '$urlPart'")

                val urls = urlPart.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d(TAG, "🔍 파싱된 URL 목록: $urls")

                Triple(MessageType.IMAGE, "", urls)
            }
            else -> {
                Log.d(TAG, "🔍 TEXT 메시지로 처리: '$message'")
                Triple(MessageType.TEXT, message, emptyList())
            }
        }
    }

    // 이미지 업로드 상태 enum
    sealed class ImageUploadStatus {
        object Idle : ImageUploadStatus()
        object Uploading : ImageUploadStatus()
        object Success : ImageUploadStatus()
        data class Error(val message: String) : ImageUploadStatus()
    }

    // 메시지 타입 enum
    enum class MessageType {
        TEXT, IMAGE
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
            // 연결 재시도
            ensureConnection()
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
            Log.d(TAG, "📤 메시지 전송 요청 완료: $message")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 메시지 전송 실패", e)
            addSystemMessage("메시지 전송 실패: ${e.message}")
        }
    }

    private fun scheduleReadMarkUpdate() {
        if (lastReadMessageId <= 0) {
            Log.d(TAG, "📖 읽음 처리할 메시지가 없음")
            return
        }

        // 기존 작업 취소
        readMarkJob?.cancel()

        // 새로운 읽음 처리 작업 예약
        readMarkJob = viewModelScope.launch {
            delay(READ_MARK_DELAY)
            performReadMarkUpdate()
        }
    }

    private fun performReadMarkUpdate() {
        if (!_connectionStatus.value) {
            Log.w(TAG, "⚠️ 연결되지 않은 상태 - 읽음 처리 연기")
            return
        }

        if (!isScreenVisible) {
            Log.d(TAG, "📖 화면이 보이지 않음 - 읽음 처리 생략")
            return
        }

        val targetMessageId = getTargetReadMessageId()

        if (targetMessageId > 0) {
            Log.d(TAG, "📖 읽음 처리 실행: targetMessageId=$targetMessageId")

            val readDTO = ChatReadDTO(
                chatRoomId = currentChatRoomId,
                userId = currentUserId,
                lastReadCid = targetMessageId
            )

            webSocketManager.markAsRead(readDTO)
        }
    }

    private fun getTargetReadMessageId(): Long {
        // 상대방이 보낸 메시지 중 가장 최신 메시지 ID
        val latestOpponentMessageId = _messages.value
            .filter { !it.isFromMe && it.id != null && it.id > 0 }
            .maxByOrNull { it.id!! }
            ?.id ?: 0L

        return maxOf(latestOpponentMessageId, lastReadMessageId)
    }

    // 이미지 전송 함수
    fun sendImageMessage(imageUris: List<Uri>) {
        if (imageUris.isEmpty()) return

        if (!_connectionStatus.value) {
            Log.w(TAG, "⚠️ 연결되지 않은 상태에서 이미지 전송 시도")
            addSystemMessage("연결되지 않았습니다. 연결을 확인해주세요.")
            ensureConnection()
            return
        }

        viewModelScope.launch {
            try {
                _imageUploadStatus.value = ImageUploadStatus.Uploading
                Log.d(TAG, "📷 이미지 업로드 시작: ${imageUris.size}개")

                val imageUrls = imageRepository.uploadImages(imageUris)
                Log.d(TAG, "✅ 이미지 업로드 완료: $imageUrls")

                // 이미지 URL들을 헤더와 함께 메시지로 전송
                val imageMessage = "IMAGE:" + imageUrls.joinToString(",")

                val messageDTO = ChatMessageDTO(
                    chatRoomId = currentChatRoomId,
                    userId = currentUserId,
                    message = imageMessage,
                    imageUrls = emptyList()
                )

                webSocketManager.sendMessage(messageDTO)
                _imageUploadStatus.value = ImageUploadStatus.Success
                Log.d(TAG, "📤 이미지 메시지 전송 완료")

            } catch (e: Exception) {
                Log.e(TAG, "❌ 이미지 전송 실패", e)
                _imageUploadStatus.value = ImageUploadStatus.Error(e.message ?: "이미지 전송 실패")
                addSystemMessage("이미지 전송에 실패했습니다: ${e.message}")
            }
        }
    }

    // ⭐ 화면 생명주기 관리 메서드들
    fun onScreenVisible() {
        Log.d(TAG, "👀 화면 표시됨 - 읽음 처리 활성화")
        isScreenVisible = true

        // 연결 상태 확인 및 재연결
        if (!webSocketManager.isConnected()) {
            Log.d(TAG, "🔌 화면 표시 시 연결 끊김 감지 - 재연결")
            ensureConnection()
        } else if (!webSocketManager.isSubscribedToRoom(currentChatRoomId)) {
            Log.d(TAG, "📡 화면 표시 시 구독 끊김 감지 - 재구독")
            webSocketManager.subscribeToChatRoom(currentChatRoomId)
        }

        // 읽음 처리 실행
        scheduleReadMarkUpdate()
    }

    fun onScreenHidden() {
        Log.d(TAG, "🙈 화면 숨겨짐 - 읽음 처리 비활성화")
        isScreenVisible = false
        readMarkJob?.cancel()
    }

    private fun loadInitialMessages() {
        Log.d(TAG, "📥 초기 메시지 로드 시작")

        viewModelScope.launch {
            try {
                val result = chatRepository.getChatMessages(currentChatRoomId)
                result.onSuccess { messageDTOs ->
                    val chatMessages = messageDTOs.map { dto ->
                        val (messageType, displayContent, imageUrls) = parseMessage(dto.message)
                        ChatMessage(
                            id = dto.chatId,
                            content = displayContent,
                            messageType = messageType,
                            imageUrls = imageUrls,
                            isFromMe = dto.userId == currentUserId,
                            timestamp = formatToHHmm(dto.createdAt ?: ""),
                            isRead = true // 기존 메시지들은 읽음 처리
                        )
                    }

                    // UI 업데이트 (메인 스레드에서)
                    launch(Dispatchers.Main) {
                        _messages.value = chatMessages
                    }

                    // 최신 상대방 메시지 ID 저장
                    val latestOpponentMessageId = messageDTOs
                        .filter { it.userId != currentUserId }
                        .maxByOrNull { it.chatId ?: 0L }
                        ?.chatId ?: 0L

                    if (latestOpponentMessageId > 0) {
                        lastReadMessageId = latestOpponentMessageId
                        Log.d(TAG, "📝 초기 로드 - 최신 상대방 메시지 ID: $lastReadMessageId")
                    }

                    Log.d(TAG, "✅ 초기 메시지 로드 완료: ${chatMessages.size}개")
                    chatMessages.forEach { message ->
                        if (message.messageType == MessageType.IMAGE) {
                            Log.d(TAG, "🖼️ 이미지 메시지 로드됨: urls=${message.imageUrls}")
                        }
                    }
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
            Log.d(TAG, "🧹 ViewModel 정리 시작")

            isScreenVisible = false
            readMarkJob?.cancel()
            connectionMonitorJob?.cancel()
            webSocketManager.disconnect()

            Log.d(TAG, "✅ ViewModel 정리 완료")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ViewModel 정리 중 오류", e)
        }
    }
}