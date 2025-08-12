package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetProfileUiState(
    val petName: String = "",
    val breed: String = "",
    val showBreedMenu: Boolean = false,
    val gender: String? = null,
    val neutered: Boolean = false,
    val birthDate: String = "",
    val age: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val breedOptions: List<String> = listOf("푸들", "말티즈", "시바견", "골든 리트리버")
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    // TODO: Add repositories when they're implemented
    // private val petRepository: PetRepository,
    // private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    fun updatePetName(name: String) {
        _uiState.value = _uiState.value.copy(
            petName = name,
            validationErrors = _uiState.value.validationErrors - "petName"
        )
    }

    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(
            breed = breed,
            showBreedMenu = false,
            validationErrors = _uiState.value.validationErrors - "breed"
        )
    }

    fun toggleBreedMenu() {
        _uiState.value = _uiState.value.copy(
            showBreedMenu = !_uiState.value.showBreedMenu
        )
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(
            gender = gender,
            validationErrors = _uiState.value.validationErrors - "gender"
        )
    }

    fun updateNeutered(neutered: Boolean) {
        _uiState.value = _uiState.value.copy(neutered = neutered)
    }

    fun updateBirthDate(date: String) {
        _uiState.value = _uiState.value.copy(
            birthDate = date,
            validationErrors = _uiState.value.validationErrors - "birthDate"
        )
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(
            age = age,
            validationErrors = _uiState.value.validationErrors - "age"
        )
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.petName.isBlank()) {
            errors["petName"] = "반려동물 이름을 입력해주세요."
        }

        if (state.breed.isBlank()) {
            errors["breed"] = "견종을 선택해주세요."
        }

        if (state.gender == null) {
            errors["gender"] = "성별을 선택해주세요."
        }

        if (state.age.isBlank()) {
            errors["age"] = "나이를 입력해주세요."
        } else {
            try {
                val ageInt = state.age.toInt()
                if (ageInt < 0 || ageInt > 30) {
                    errors["age"] = "올바른 나이를 입력해주세요. (0-30)"
                }
            } catch (e: NumberFormatException) {
                errors["age"] = "나이는 숫자로 입력해주세요."
            }
        }

        _uiState.value = state.copy(validationErrors = errors)
        return errors.isEmpty()
    }

    fun savePetProfile(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                // TODO: Implement actual pet profile save logic
                // val petProfile = PetProfile(...)
                // petRepository.savePetProfile(petProfile)
                // if image exists, imageRepository.uploadPetImage(...)

                // Simulate network call
                kotlinx.coroutines.delay(1500)

                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "펫 프로필 저장 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}