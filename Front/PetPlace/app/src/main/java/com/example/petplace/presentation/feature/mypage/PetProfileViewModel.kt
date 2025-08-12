package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.PetRepository
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
    val breedOptions: List<String> = listOf("푸들", "말티즈", "시바견", "골든 리트리버"),
    val petId: Int? = null,  // 수정 모드일 때 사용
    val isEditMode: Boolean = false,
)

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    // 기존 펫 정보 로드 (수정 모드용)
    fun loadPetInfo(petId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // MyPageRepository의 getMyPageInfo()에서 petList 가져오기
                // 실제로는 개별 펫 정보를 가져오는 API가 필요할 수 있음

                _uiState.value = _uiState.value.copy(
                    petId = petId,
                    isEditMode = true,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "펫 정보를 불러오는데 실패했습니다."
                )
            }
        }
    }


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

                val state = _uiState.value

                // 성별을 API 형식에 맞게 변환
                val apiGender = when(state.gender) {
                    "남아" -> "MALE"
                    "여아" -> "FEMALE"
                    else -> "MALE"
                }

                val result = petRepository.savePetInfo(
                    petId = state.petId,  // null이면 추가, 값이 있으면 수정
                    name = state.petName,
                    animal = "DOG", // 현재는 고정, 필요시 UI에서 선택하도록 확장
                    breed = mapBreedToApiFormat(state.breed), // 견종 매핑 필요
                    sex = apiGender,
                    birthday = formatBirthDateForApi(state.birthDate), // "2025-08-12" 형식으로 변환
                    imgSrc = state.profileImageUri?.toString(),
                    tnr = state.neutered
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSaving = false)
                        onSuccess()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = exception.message ?: "펫 프로필 저장에 실패했습니다."
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "펫 프로필 저장 중 오류가 발생했습니다."
                )
            }
        }
    }

    // 헬퍼 메서드들 추가
    private fun mapBreedToApiFormat(breed: String): String {
        return when(breed) {
            "푸들" -> "POODLE"
            "말티즈" -> "MALTESE"
            "시바견" -> "SHIBA_INU"
            "골든 리트리버" -> "GOLDEN_RETRIEVER"
            else -> "MIXED" // 기본값
        }
    }

    private fun formatBirthDateForApi(dateStr: String): String {
        // "mm/dd/yyyy" -> "yyyy-mm-dd" 형식으로 변환
        if (dateStr.isBlank()) return ""

        val parts = dateStr.split("/")
        if (parts.size == 3) {
            val month = parts[0].padStart(2, '0')
            val day = parts[1].padStart(2, '0')
            val year = parts[2]
            return "$year-$month-$day"
        }
        return dateStr
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}