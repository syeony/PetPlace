package com.example.petplace.presentation.feature.mypage

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.TokenApiService
import com.example.petplace.data.repository.MyPageRepository
import com.example.petplace.data.repository.ImageRepository
import com.example.petplace.util.CommonUtils
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
    val userImgSrc: String = ""
)

data class PetInfo(
    val id: Int,
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val age: Int = 0,
    val imgSrc: String? = ""
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
    private val myPageRepository: MyPageRepository,
    private val imageRepository: ImageRepository, // ImageRepository 추가
    private val tokenApiService: TokenApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun refreshData() {
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
                                id = pet.id,
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
                            petSupplies = petSupplies, // petSupplies 업데이트 추가
                            isLoading = false
                        )
                        Log.d("MyPage", "after uiState update")
                        Log.d("MyPage", "nickname=${response.nickname}")
                        Log.d("MyPage", "petSupplies loaded: $petSupplies")
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
            }
        }

        return PetSupplyState(
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

    fun logout() {
        viewModelScope.launch {
            try {
                val app = PetPlaceApp.getAppContext() as? PetPlaceApp
                app?.clearLoginData()
                val fcmToken = CommonUtils.getFcmToken()
                if (fcmToken != null) {
                    tokenApiService.deactivateFcmToken(fcmToken)
                }
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
                val imageUri = currentState.selectedSupplyImage ?: return@launch

                _uiState.value = _uiState.value.copy(isSavingSupply = true)

                // 1단계: 이미지 업로드 (채팅에서 사용하는 방식과 동일)
                Log.d("MyPage", "이미지 업로드 시작: $imageUri")
                val uploadedImageUrls = imageRepository.uploadImages(listOf(imageUri))

                if (uploadedImageUrls.isEmpty()) {
                    throw Exception("이미지 업로드에 실패했습니다.")
                }

                val serverImageUrl = uploadedImageUrls.first()
                Log.d("MyPage", "이미지 업로드 완료: $serverImageUrl")

                // 2단계: 서버에 용품 정보 저장
                val hasExistingImage = when (supplyType) {
                    SupplyType.BATH -> !currentState.petSupplies.bathImageUrl.isNullOrEmpty()
                    SupplyType.FOOD -> !currentState.petSupplies.foodImageUrl.isNullOrEmpty()
                    SupplyType.WASTE -> !currentState.petSupplies.wasteImageUrl.isNullOrEmpty()
                }

                Log.d("MyPage", "서버에 용품 정보 저장: $supplyType, hasExisting: $hasExistingImage")

                myPageRepository.savePetSupplyInfo(supplyType, serverImageUrl, hasExistingImage)
                    .onSuccess { response ->
                        Log.d("MyPage", "용품 저장 성공: $response")

                        // 3단계: UI 상태 업데이트 (서버 URL 사용)
                        val updatedSupplies = when (supplyType) {
                            SupplyType.BATH -> currentState.petSupplies.copy(bathImageUrl = serverImageUrl)
                            SupplyType.FOOD -> currentState.petSupplies.copy(foodImageUrl = serverImageUrl)
                            SupplyType.WASTE -> currentState.petSupplies.copy(wasteImageUrl = serverImageUrl)
                        }

                        _uiState.value = _uiState.value.copy(
                            petSupplies = updatedSupplies,
                            isSavingSupply = false,
                            showSupplyDialog = false, // 다이얼로그 닫기
                            selectedSupplyImage = null // 선택된 이미지 초기화
                        )

                        Log.d("MyPage", "UI 업데이트 완료: ${updatedSupplies}")
                    }
                    .onFailure { exception ->
                        Log.e("MyPage", "용품 저장 실패", exception)
                        _uiState.value = _uiState.value.copy(
                            error = "용품 저장에 실패했습니다: ${exception.message}",
                            isSavingSupply = false
                        )
                    }

            } catch (e: Exception) {
                Log.e("MyPage", "용품 저장 중 오류", e)
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

    private fun mapApiGenderToDisplay(apiGender: String): String {
        return when(apiGender) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "남아"
        }
    }
}