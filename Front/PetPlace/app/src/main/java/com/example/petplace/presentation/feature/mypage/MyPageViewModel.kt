package com.example.petplace.presentation.feature.mypage

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val nickname: String = "",
    val location: String = "",
    val level: Int = 1,
    val experienceProgress: Float = 0f,
    val introduction: String = "",
    val userImgSrc: String = ""  // 추가
)

data class PetInfo(
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val age: Int = 0,
    val imgSrc: String? = ""  // 추가
)

data class PetSupplyState(
    val bathImageUrl: String? = null,
    val foodImageUrl: String? = null,
    val wasteImageUrl: String? = null
)

data class MyPageUiState(
    val userProfile: UserProfileState = UserProfileState(),
    val pets: List<PetInfo> = emptyList(),
    val petSupplies: PetSupplyState = PetSupplyState(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val showSupplyDialog: Boolean = false,
    val currentSupplyType: SupplyType? = null,
    val selectedSupplyImage: Uri? = null,
    val isSavingSupply: Boolean = false
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                myPageRepository.getMyPageInfo()
                    .onSuccess { response ->
                        Log.d(TAG, "loadUserProfile: $response")
                        Log.d("MyPage", "nickname=${response.nickname}")
                        val userProfile = UserProfileState(
                            nickname = response.nickname ?: "",
                            location = response.regionName ?: "",
                            level = response.level,
                            experienceProgress = response.experience / 100f,
                            introduction = response.introduction ?: "소개글이 없습니다.",
                            userImgSrc = response.userImgSrc ?: ""
                        )

                        val pets = response.petList?.map { pet ->
                            PetInfo(
                                name = pet.name,
                                breed = pet.breed,
                                gender = pet.sex,
                                age = calculateAge(pet.birthday),
                                imgSrc = pet.imgSrc
                            )
                        } ?: emptyList()

                        val petSupplies = parsePetSupplies(response.imgList)

                        _uiState.value = _uiState.value.copy(
                            userProfile = userProfile,
                            pets = pets,
                            isLoading = false
                        )
                        Log.d("MyPage", "after uiState update")
                        Log.d("MyPage", "nickname=${response.nickname}")
                    }
                    .onFailure { exception ->
                        Log.e("MyPage", "getMyPageInfo failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun parsePetSupplies(imgList: List<com.example.petplace.data.model.mypage.MyPageInfoResponse.ImageInfo>): PetSupplyState {
        var bathImageUrl: String? = null
        var foodImageUrl: String? = null
        var wasteImageUrl: String? = null

        imgList.forEach { imageInfo ->
            when (imageInfo.sort) {
                1 -> bathImageUrl = imageInfo.src  // 목욕 용품
                2 -> foodImageUrl = imageInfo.src  // 사료 용품
                3 -> wasteImageUrl = imageInfo.src // 배변 용품
                // 필요에 따라 다른 sort 값들도 추가
            }
        }

        return PetSupplyState(
            bathImageUrl = bathImageUrl,
            foodImageUrl = foodImageUrl,
            wasteImageUrl = wasteImageUrl
        )
    }

    private fun calculateAge(birthday: String): Int {
        // birthday 형식에 따라 나이 계산 로직 구현
        // 예: "2020-01-01" 형식이라면
        return try {
            val birthYear = birthday.substring(0, 4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - birthYear
        } catch (e: Exception) {
            0
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val app = PetPlaceApp.getAppContext() as? PetPlaceApp
                app?.clearLoginData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showSupplyDialog(supplyType: SupplyType) {
        _uiState.value = _uiState.value.copy(
            showSupplyDialog = true,
            currentSupplyType = supplyType
        )
    }

    fun hideSupplyDialog() {
        _uiState.value = _uiState.value.copy(
            showSupplyDialog = false,
            currentSupplyType = null,
            selectedSupplyImage = null
        )
    }

    fun updateSupplyImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedSupplyImage = uri
        )
    }

    fun saveSupplyInfo() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val supplyType = currentState.currentSupplyType ?: return@launch
                val imageUri = currentState.selectedSupplyImage?.toString() ?: return@launch

                _uiState.value = _uiState.value.copy(isSavingSupply = true)

                // 기존 이미지가 있는지 확인
                val hasExistingImage = when (supplyType) {
                    SupplyType.BATH -> !currentState.petSupplies.bathImageUrl.isNullOrEmpty()
                    SupplyType.FOOD -> !currentState.petSupplies.foodImageUrl.isNullOrEmpty()
                    SupplyType.WASTE -> !currentState.petSupplies.wasteImageUrl.isNullOrEmpty()
                }

                Log.d("MyPage", "Saving supply: $supplyType, hasExisting: $hasExistingImage")

                myPageRepository.savePetSupplyInfo(supplyType, imageUri, hasExistingImage)
                    .onSuccess { response ->
                        Log.d("MyPage", "Supply saved successfully: $response")

                        // UI 상태 업데이트
                        val updatedSupplies = when (supplyType) {
                            SupplyType.BATH -> currentState.petSupplies.copy(bathImageUrl = imageUri)
                            SupplyType.FOOD -> currentState.petSupplies.copy(foodImageUrl = imageUri)
                            SupplyType.WASTE -> currentState.petSupplies.copy(wasteImageUrl = imageUri)
                        }

                        _uiState.value = _uiState.value.copy(
                            petSupplies = updatedSupplies,
                            isSavingSupply = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MyPage", "Failed to save supply", exception)
                        _uiState.value = _uiState.value.copy(
                            error = "용품 저장에 실패했습니다: ${exception.message}",
                            isSavingSupply = false
                        )
                    }

            } catch (e: Exception) {
                Log.e("MyPage", "Error saving supply", e)
                _uiState.value = _uiState.value.copy(
                    error = "용품 저장 중 오류가 발생했습니다: ${e.message}",
                    isSavingSupply = false
                )
            }
        }
    }

    // 특정 용품 타입의 기존 이미지 URL 가져오기
    fun getExistingSupplyImageUrl(supplyType: SupplyType): String? {
        return when (supplyType) {
            SupplyType.BATH -> _uiState.value.petSupplies.bathImageUrl
            SupplyType.FOOD -> _uiState.value.petSupplies.foodImageUrl
            SupplyType.WASTE -> _uiState.value.petSupplies.wasteImageUrl
        }
    }
}