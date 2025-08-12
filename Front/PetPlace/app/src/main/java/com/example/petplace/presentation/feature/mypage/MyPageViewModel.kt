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

data class MyPageUiState(
    val userProfile: UserProfileState = UserProfileState(),
    val pets: List<PetInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val showSupplyDialog: Boolean = false,
    val currentSupplyType: SupplyType? = null,
    val selectedSupplyImage: Uri? = null
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
                            introduction = response.introduction ?: "소개글이 없습니다."
                        )

                        val pets = response.petList?.map { pet ->
                            PetInfo(
                                name = pet.name,
                                breed = pet.breed,
                                gender = pet.sex,
                                age = calculateAge(pet.birthday)
                            )
                        } ?: emptyList()


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
                // 서버에 용품 정보 저장 로직
                val supplyType = _uiState.value.currentSupplyType
                val imageUri = _uiState.value.selectedSupplyImage

                // TODO: Repository를 통해 서버에 저장
                // myPageRepository.saveSupplyInfo(supplyType, imageUri)

                Log.d("MyPage", "Supply saved: $supplyType")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}