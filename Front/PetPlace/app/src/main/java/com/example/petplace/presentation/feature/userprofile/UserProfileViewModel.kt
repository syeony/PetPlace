package com.example.petplace.presentation.feature.userprofile

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UserProfileInfo(
    val userId: Long,
    val nickname: String = "",
    val location: String = "",
    val level: Int = 1,
    val experienceProgress: Float = 0f,
    val introduction: String = "",
    val userImgSrc: String = ""
)

data class UserPetInfo(
    val id: Long,
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val age: Int = 0,
    val imgSrc: String? = ""
)

data class UserPetSupplies(
    val bathImageUrl: String? = null,
    val foodImageUrl: String? = null,
    val wasteImageUrl: String? = null
)

data class UserProfileUiState(
    val userProfile: UserProfileInfo = UserProfileInfo(0),
    val pets: List<UserPetInfo> = emptyList(),
    val petSupplies: UserPetSupplies = UserPetSupplies(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdChatRoomId: Long? = null,
    val isChatRoomCreating: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val chatApiService: ChatApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                myPageRepository.getUserProfile(userId)
                    .onSuccess { response ->
                        Log.d("UserProfile", "loadUserProfile success: $response")

                        val userProfile = UserProfileInfo(
                            userId = response.userId,
                            nickname = response.nickname ?: "",
                            location = response.regionName ?: "",
                            level = response.level,
                            experienceProgress = (response.experience % 100) / 100f, // 경험치를 0~1 범위로 변환
                            introduction = response.introduction ?: "소개글이 없습니다.",
                            userImgSrc = response.userImgSrc ?: ""
                        )

                        val pets = response.petList?.map { pet ->
                            UserPetInfo(
                                id = pet.id.toLong(),
                                name = pet.name,
                                breed = pet.breed,
                                gender = mapApiGenderToDisplay(pet.sex),
                                age = calculateAge(pet.birthday),
                                imgSrc = pet.imgSrc
                            )
                        } ?: emptyList()

                        val petSupplies = parsePetSupplies(response.imgList)

                        _uiState.value = _uiState.value.copy(
                            userProfile = userProfile,
                            pets = pets,
                            petSupplies = petSupplies,
                            isLoading = false,
                            error = null
                        )

                        Log.d("UserProfile", "UI state updated successfully")
                    }
                    .onFailure { exception ->
                        Log.e("UserProfile", "getUserProfile failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Log.e("UserProfile", "loadUserProfile error", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun parsePetSupplies(imgList: List<com.example.petplace.data.model.mypage.UserProfileResponse.ImageInfo>): UserPetSupplies {
        var bathImageUrl: String? = null
        var foodImageUrl: String? = null
        var wasteImageUrl: String? = null

        imgList.forEach { imageInfo ->
            when (imageInfo.sort) {
                1 -> bathImageUrl = imageInfo.src  // 목욕 용품
                2 -> foodImageUrl = imageInfo.src  // 사료 용품
                3 -> wasteImageUrl = imageInfo.src // 배변 용품
            }
        }

        return UserPetSupplies(
            bathImageUrl = bathImageUrl,
            foodImageUrl = foodImageUrl,
            wasteImageUrl = wasteImageUrl
        )
    }

    private fun calculateAge(birthday: String): Int {
        return try {
            val birthYear = birthday.substring(0, 4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - birthYear
        } catch (e: Exception) {
            0
        }
    }

    private fun mapApiGenderToDisplay(apiGender: String): String {
        return when(apiGender) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> apiGender
        }
    }

    // 채팅방 생성을 위한 간단한 함수 추가
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

    // 채팅방 ID 소비 후 초기화
    fun consumeCreatedChatRoomId() {
        _uiState.value = _uiState.value.copy(createdChatRoomId = null)
    }

    // 기존 createChatRoom 함수를 private로 변경
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}