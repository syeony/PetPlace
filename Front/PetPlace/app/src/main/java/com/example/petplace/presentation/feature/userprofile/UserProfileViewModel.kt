package com.example.petplace.presentation.feature.userprofile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.MyPageRepository
//import com.example.petplace.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository
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
            else -> "남아"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}