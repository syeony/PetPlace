package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditUiState(
    val userName: String = "",
    val nickname: String = "",
    val profileImageUri: Uri? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPwVisible: Boolean = false,
    val newPwVisible: Boolean = false,
    val confirmPwVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val passwordValidationError: String? = null
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    // TODO: Add repositories when they're implemented
    // private val userRepository: UserRepository,
    // private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUserData()
    }

    private fun loadCurrentUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val app = PetPlaceApp.getAppContext() as? PetPlaceApp
                val userInfo = app?.getUserInfo()

                _uiState.value = _uiState.value.copy(
                    userName = userInfo?.userName ?: "",
                    nickname = userInfo?.nickname ?: "",
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

    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname)
    }

    fun updateCurrentPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password,
            passwordValidationError = null
        )
    }

    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            passwordValidationError = null
        )
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password,
            passwordValidationError = null
        )
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            currentPwVisible = !_uiState.value.currentPwVisible
        )
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            newPwVisible = !_uiState.value.newPwVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            confirmPwVisible = !_uiState.value.confirmPwVisible
        )
    }

    private fun validatePasswordChange(): Boolean {
        val state = _uiState.value

        if (state.newPassword.isNotEmpty() || state.confirmPassword.isNotEmpty()) {
            if (state.currentPassword.isEmpty()) {
                _uiState.value = state.copy(
                    passwordValidationError = "현재 비밀번호를 입력해주세요."
                )
                return false
            }

            if (state.newPassword.isEmpty()) {
                _uiState.value = state.copy(
                    passwordValidationError = "새 비밀번호를 입력해주세요."
                )
                return false
            }

            if (state.newPassword != state.confirmPassword) {
                _uiState.value = state.copy(
                    passwordValidationError = "새 비밀번호가 일치하지 않습니다."
                )
                return false
            }

            if (state.newPassword.length < 6) {
                _uiState.value = state.copy(
                    passwordValidationError = "비밀번호는 최소 6자 이상이어야 합니다."
                )
                return false
            }
        }

        return true
    }

    fun saveProfile(onSuccess: () -> Unit) {
        if (!validatePasswordChange()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                // TODO: Implement actual profile update logic
                // userRepository.updateProfile(...)
                // if password change requested, userRepository.changePassword(...)
                // if image changed, imageRepository.uploadImage(...)

                // Simulate network call
                kotlinx.coroutines.delay(1000)

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "프로필이 성공적으로 업데이트되었습니다."
                )

                onSuccess()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "프로필 업데이트 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
            passwordValidationError = null
        )
    }
}