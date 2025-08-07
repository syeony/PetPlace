package com.example.petplace.presentation.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.local.chat.ChatRoom
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

    init {
        Log.d(TAG, "ChatListViewModel 초기화")
        loadChatRooms()
    }

    fun loadChatRooms() {
        Log.d(TAG, "채팅방 목록 로드 시작: userId=$currentUserId")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = chatRepository.getChatRooms(currentUserId)

                result.onSuccess { chatRoomResponses ->
                    Log.d(TAG, "채팅방 목록 로드 성공: ${chatRoomResponses.size}개")

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

    private suspend fun convertToChatRoom(response: ChatRoomResponse): ChatRoom {
        // 상대방 ID 찾기
        val partnerId = if (response.userId1 == currentUserId) {
            response.userId2
        } else {
            response.userId1
        }

        // 상대방 정보 가져오기 (임시로 하드코딩)
        val partnerInfo = getUser(partnerId)

        // 안 읽은 메시지 수 가져오기
        val unreadCount = chatRepository.getUnreads(response.chatRoomId, currentUserId)
            .getOrElse {
                Log.w(TAG, "안 읽은 메시지 수 가져오기 실패: ${it.message}")
                0 // 실패 시 0으로 설정
            }

        Log.d(TAG, "채팅방 변환: chatRoomId=${response.chatRoomId}, partner=${partnerInfo.name}")

        return ChatRoom(
            id = response.chatRoomId,
            name = partnerInfo.name,
            region = partnerInfo.region ?: "알 수 없음",
            lastMessage = response.lastMessage ?: "아직 메시지가 없습니다.",
            time = formatLastMessageTime(response.lastMessageAt),
            unreadCount = unreadCount, // 일단 0으로 설정 (나중에 API 추가 시 수정)
            profileImageUrl = partnerInfo.profileImageUrl
        )
    }

    private fun formatLastMessageTime(lastMessageAt: String?): String {
        if (lastMessageAt == null) {
            return ""
        }

        try {
            // ISO 8601 형식의 시간을 파싱해서 표시 형식으로 변환
            // 실제로는 더 정교한 시간 포맷팅이 필요함
            // 예: "2025-08-05T06:23:23.635Z" -> "오전 6:23" 또는 "8월 5일"

            // 임시로 간단한 형식으로 반환
            return "최근"
        } catch (e: Exception) {
            Log.w(TAG, "시간 포맷팅 실패: $lastMessageAt", e)
            return ""
        }
    }

    // 임시 사용자 정보 (실제로는 UserRepository에서 가져와야 함)
    private fun getUser(userId: Long): User {
        return when (userId) {
            3L -> User(3L, "김철수", com.example.petplace.R.drawable.ic_mypage, "인의동")
            6L -> User(6L, "나", com.example.petplace.R.drawable.ic_mypage, "진평동")
            else -> User(userId, "사용자$userId", com.example.petplace.R.drawable.ic_mypage, "알 수 없음")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshChatRooms() {
        Log.d(TAG, "채팅방 목록 새로고침")
        loadChatRooms()
    }
}

// 사용자 정보 (임시)
data class User(
    val userId: Long,
    val name: String,
    val profileImageUrl: Int? = null,
    val region: String? = null
)