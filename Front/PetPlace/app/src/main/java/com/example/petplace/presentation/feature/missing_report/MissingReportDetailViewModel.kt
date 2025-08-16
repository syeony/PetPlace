package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.model.missing_report.MissingReportDetailDto
import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MissingReportDetailViewModel @Inject constructor(
    private val repo: MissingSightingRepository,
    private val chatApiService: ChatApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val app = context as PetPlaceApp
    private val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    data class UiState(
        val loading: Boolean = false,
        val data: MissingReportDetailDto? = null,
        val error: String? = null,
        val createdChatRoomId: Long? = null,
        val isChatRoomCreating: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun load(id: Long) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            repo.getMissingReportDetail(id)
                .onSuccess { detail ->
                    _ui.update { it.copy(loading = false, data = detail) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message ?: "불러오기 실패") }
                }
        }
    }

    fun startChatWithUser(userId: Long) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isChatRoomCreating = true)

                val result = createChatRoom(userId)
                result.onSuccess { chatRoomResponse ->
                    _ui.value = _ui.value.copy(
                        createdChatRoomId = chatRoomResponse.chatRoomId,
                        isChatRoomCreating = false
                    )
                }.onFailure { exception ->
                    _ui.value = _ui.value.copy(
                        error = exception.message ?: "채팅방 생성에 실패했습니다.",
                        isChatRoomCreating = false
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    error = e.message ?: "채팅방 생성 중 오류가 발생했습니다.",
                    isChatRoomCreating = false
                )
            }
        }
    }

    private suspend fun createChatRoom(userId: Long): Result<ChatRoomResponse> {
        val myId = user.userId ?: 0
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.createChatRoom(
                    CreateChatRoomRequest(userId1 = myId, userId2 = userId)
                )

                if (response.isSuccessful) {
                    val chatRoom = response.body()
                    if (chatRoom != null) {
                        Result.success(chatRoom)
                    } else {
                        Result.failure(Exception("채팅방 생성 응답이 null"))
                    }
                } else {
                    Result.failure(Exception("채팅방 생성 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun consumeCreatedChatRoomId() {
        _ui.value = _ui.value.copy(createdChatRoomId = null)
    }
}
