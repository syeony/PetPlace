package com.example.petplace.presentation.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatRoom
import com.example.petplace.data.model.chat.ChatPartnerResponse
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatListViewModel"
    }

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    private val currentUserId = userInfo?.userId ?: 0

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isCreatingChatRoom = MutableStateFlow(false)
    val isCreatingChatRoom: StateFlow<Boolean> = _isCreatingChatRoom.asStateFlow()

    private val _chatRoomCreated = MutableStateFlow<ChatRoomResponse?>(null)
    val chatRoomCreated: StateFlow<ChatRoomResponse?> = _chatRoomCreated.asStateFlow()

    fun loadChatRooms() {
        Log.d(TAG, "채팅방 목록 로드 시작: userId=$currentUserId")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = chatRepository.getChatRooms(currentUserId)

                result.onSuccess { chatRoomResponses ->
                    Log.d(TAG, "채팅방 목록 로드 성공: ${chatRoomResponses.size}개")
                    Log.d(TAG, "loadChatRooms: ${chatRoomResponses}")
                    // ChatRoomResponse를 ChatRoom으로 변환
                    val chatRoomList = chatRoomResponses.map { response ->
                        convertToChatRoom(response)
                    }

                    _chatRooms.value = chatRoomList
                    Log.d(TAG, "채팅방 목록 변환 완료")

                }.onFailure { exception ->
                    Log.e(TAG, "채팅방 목록 로드 실패", exception)
                    _errorMessage.value = "채팅방 목록을 불러오는데 실패했습니다: ${exception.message}"

                    // 실패 시 빈 목록으로 설정
                    _chatRooms.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e(TAG, "채팅방 목록 로드 중 예외 발생", e)
                _errorMessage.value = "예상치 못한 오류가 발생했습니다: ${e.message}"
                _chatRooms.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createChatRoom(userId1: String, userId2: String) {
        viewModelScope.launch {
            _isCreatingChatRoom.value = true
            _errorMessage.value = null

            try {
                val user1Id = userId1.toLongOrNull()
                val user2Id = userId2.toLongOrNull()

                if (user1Id == null || user2Id == null) {
                    _errorMessage.value = "올바른 사용자 ID를 입력해주세요"
                    return@launch
                }

                Log.d(TAG, "채팅방 생성 요청: userId1=$user1Id, userId2=$user2Id")

                val result = chatRepository.createChatRoom(user1Id, user2Id)

                result.onSuccess { chatRoomResponse ->
                    Log.d(TAG, "채팅방 생성 성공: ${chatRoomResponse.chatRoomId}")
                    _chatRoomCreated.value = chatRoomResponse

                    // 채팅방 생성 후 목록 새로고침
                    loadChatRooms()

                }.onFailure { exception ->
                    Log.e(TAG, "채팅방 생성 실패", exception)
                    _errorMessage.value = "채팅방 생성에 실패했습니다: ${exception.message}"
                }

            } catch (e: Exception) {
                Log.e(TAG, "채팅방 생성 중 예외 발생", e)
                _errorMessage.value = "예상치 못한 오류가 발생했습니다: ${e.message}"
            } finally {
                _isCreatingChatRoom.value = false
            }
        }
    }

    private suspend fun convertToChatRoom(response: ChatRoomResponse): ChatRoom {
        // 상대방 ID 찾기
        val partnerId = if (response.userId1 == currentUserId) {
            response.userId2
        } else {
            response.userId1
        }

        // 상대방 정보 가져오기
        val partnerInfo = getPartner(response.chatRoomId, partnerId)

        // 안 읽은 메시지 수 가져오기
        val unreadCount = chatRepository.getUnreads(response.chatRoomId, currentUserId)
            .getOrElse {
                Log.w(TAG, "안 읽은 메시지 수 가져오기 실패: ${it.message}")
                0 // 실패 시 0으로 설정
            }

        Log.d(TAG, "채팅방 변환: chatRoomId=${response.chatRoomId}, partner=${partnerInfo.nickname}")

        // 프로필 이미지 URL 처리 - 서버 URL 결합
        val profileImageUrl = partnerInfo.profileImageUrl?.let { imageUrl ->
            if (imageUrl.startsWith("http")) {
                // 이미 전체 URL인 경우
                imageUrl
            } else {
                // 상대 경로인 경우 서버 URL과 결합
                "http://43.201.108.195:8081$imageUrl"
            }
        }

        return ChatRoom(
            id = response.chatRoomId,
            name = partnerInfo.nickname,
            region = partnerInfo.region ?: "알 수 없음",
            lastMessage = formatLastMessage(response.lastMessage),
            time = formatLastMessageTime(response.lastMessageAt),
            unreadCount = unreadCount,
            profileImageUrl = profileImageUrl // String으로 변경
        )
    }

    private fun formatLastMessage(lastMessage: String?): String {
        if(lastMessage == null || lastMessage.isEmpty())
            return "아직 메시지가 없습니다."
        if(lastMessage.startsWith("IMAGE:"))
            return "이미지 \uD83D\uDDBC\uFE0F"
        return lastMessage
    }

    private fun formatLastMessageTime(lastMessageAt: String?): String {
        if (lastMessageAt == null) {
            return ""
        }

        try {
            // 1. UTC 기준 입력 파싱
            val inputFormat =
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // 입력은 UTC 기준

            // 2. KST 기준으로 출력 포맷 정의
            val outputFormat = java.text.SimpleDateFormat("a hh:mm", java.util.Locale("ko", "KR"))
            outputFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul") // 출력은 KST

            // 3. 파싱 및 포맷
            val date = inputFormat.parse(lastMessageAt)
            return outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.w(TAG, "시간 포맷팅 실패: $lastMessageAt", e)
            return ""
        }
    }

    private suspend fun getPartner(chatRoomId: Long, partnerId: Long): ChatPartnerResponse {
        val participants = chatRepository.getParticipants(chatRoomId).getOrThrow()
        Log.d(TAG, "partner id: $partnerId")
        val partner = participants.firstOrNull { it.userId == partnerId }
            ?: throw IllegalStateException("해당 userId에 해당하는 참가자가 존재하지 않습니다.")
        Log.d(TAG, "getPartner: $partner")
        return partner
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearChatRoomCreated() {
        _chatRoomCreated.value = null
    }

    fun refreshChatRooms() {
        Log.d(TAG, "채팅방 목록 새로고침")
        loadChatRooms()
    }
}


