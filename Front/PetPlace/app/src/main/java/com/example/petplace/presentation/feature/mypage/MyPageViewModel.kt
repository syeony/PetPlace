package com.example.petplace.presentation.feature.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val nickname: String = "",
    val location: String = "인의동",
    val level: Int = 1,
    val experienceProgress: Float = 0.6f,
    val introduction: String = "안녕하세요!\n저는 평소에 강아지에 관심이 많아서 돌봄을 많이 해 보고 싶어서 가입하게 되었습니다!\n돌봄이 필요할때 언제든 연락주세요! 그리고... 더보기"
)

data class PetInfo(
    val name: String = "두부",
    val breed: String = "말티즈",
    val gender: String = "여아",
    val age: Int = 8
)

data class MyPageUiState(
    val userProfile: UserProfileState = UserProfileState(),
    val pets: List<PetInfo> = listOf(PetInfo()),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    // TODO: Add repositories when they're implemented
    // private val userRepository: UserRepository,
    // private val petRepository: PetRepository
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

                // Get user info from app context for now
                val app = PetPlaceApp.getAppContext() as? PetPlaceApp
                val userInfo = app?.getUserInfo()

                val userProfile = UserProfileState(
                    nickname = userInfo?.nickname ?: "사용자"
                )

                _uiState.value = _uiState.value.copy(
                    userProfile = userProfile,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
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
}