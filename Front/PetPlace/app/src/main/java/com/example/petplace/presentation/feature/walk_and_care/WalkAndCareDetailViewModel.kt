package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.cares.CareDetail
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.repository.CaresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import javax.inject.Inject

private const val TAG = "WalkAndCareDetailViewModel"

data class DetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val data: CareDetail? = null,
    val createdChatRoomId: Long? = null,
    val isChatRoomCreating: Boolean = false
)

@HiltViewModel
class WalkAndCareDetailViewModel @Inject constructor(
    private val caresRepository: CaresRepository,
    private val chatApiService: ChatApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetailUiState(loading = true)
            caresRepository.detail(id)
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body?.success == true && body.data != null) {
                            _uiState.value = DetailUiState(
                                loading = false,
                                data = body.data
                            )
                        } else {
                            _uiState.value = DetailUiState(
                                loading = false,
                                error = body?.message ?: "상세 데이터를 불러올 수 없습니다."
                            )
                        }
                    } else {
                        _uiState.value = DetailUiState(
                            loading = false,
                            error = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = DetailUiState(
                        loading = false,
                        error = e.message ?: "네트워크 오류"
                    )
                }
        }
    }

    fun startChatWithUser(userId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isChatRoomCreating = true)

                val result = createChatRoom(userId)
                result.onSuccess { chatRoomResponse ->
                    _uiState.value = _uiState.value.copy(
                        createdChatRoomId = chatRoomResponse.chatRoomId,
                        isChatRoomCreating = false
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "채팅방 생성에 실패했습니다.",
                        isChatRoomCreating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "채팅방 생성 중 오류가 발생했습니다.",
                    isChatRoomCreating = false
                )
            }
        }
    }

    private suspend fun createChatRoom(userId: Long): Result<ChatRoomResponse> {
        val app = PetPlaceApp.getAppContext() as PetPlaceApp
        val userInfo = app.getUserInfo()
        val myId = userInfo?.userId ?: 0
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅방 생성: userId1=$myId, userId2=$userId")

                val response = chatApiService.createChatRoom(
                    CreateChatRoomRequest(userId1 = myId, userId2 = userId)
                )

                if (response.isSuccessful) {
                    val chatRoom = response.body()
                    if (chatRoom != null) {
                        Log.d(TAG, "채팅방 생성 성공: chatRoomId=${chatRoom.chatRoomId}")
                        Result.success(chatRoom)
                    } else {
                        Log.e(TAG, "채팅방 생성 응답이 null")
                        Result.failure(Exception("채팅방 생성 응답이 null"))
                    }
                } else {
                    Log.e(TAG, "채팅방 생성 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("채팅방 생성 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "채팅방 생성 중 오류", e)
                Result.failure(e)
            }
        }
    }

    fun consumeCreatedChatRoomId() {
        _uiState.value = _uiState.value.copy(createdChatRoomId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}