package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.ImageRepository
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
    private val petRepository: PetRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    // 기존 펫 정보 로드 (수정 모드용)
    fun loadPetInfo(petId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                petRepository.getPetInfo(petId)
                    .onSuccess { pet ->
                        // 서버 이미지 URL 처리
                        val imageUrl = if (!pet.imgSrc.isNullOrEmpty()) {
                            if (pet.imgSrc.startsWith("http")) {
                                pet.imgSrc
                            } else {
                                "http://43.201.108.195:8081${pet.imgSrc}"
                            }
                        } else null

                        _uiState.value = _uiState.value.copy(
                            petId = petId,
                            isEditMode = true,
                            petName = pet.name,
                            breed = mapApiBreedToDisplay(pet.breed),
                            gender = mapApiGenderToDisplay(pet.sex),
                            neutered = pet.tnr,
                            birthDate = formatApiDateToDisplay(pet.birthday),
                            age = calculateAge(pet.birthday).toString(),
                            profileImageUri = imageUrl?.let { Uri.parse(it) },
                            isLoading = false
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "펫 정보를 불러오는데 실패했습니다: ${exception.message}"
                        )
                    }
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

                // 1. 이미지 처리 로직 수정
                var finalImageUrl: String? = null

                if (state.profileImageUri != null) {
                    val imageUriString = state.profileImageUri.toString()

                    // 새로 선택한 이미지인지 확인 (기존 서버 URL이 아닌 경우)
                    if (!imageUriString.startsWith("http://43.201.108.195:8081")) {
                        // 새 이미지 업로드
                        try {
                            val uploadedUrls = imageRepository.uploadImages(listOf(state.profileImageUri))
                            finalImageUrl = uploadedUrls.firstOrNull()
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                error = "이미지 업로드에 실패했습니다: ${e.message}"
                            )
                            return@launch
                        }
                    } else {
                        // 기존 이미지 URL에서 서버 베이스 URL 제거하여 상대 경로로 변환
                        finalImageUrl = imageUriString.replace("http://43.201.108.195:8081", "")
                    }
                }

                // 성별을 API 형식에 맞게 변환
                val apiGender = when(state.gender) {
                    "남아" -> "MALE"
                    "여아" -> "FEMALE"
                    else -> "MALE"
                }

                // 2. 펫 정보 저장 (기존 이미지 URL도 포함)
                val result = petRepository.savePetInfo(
                    petId = state.petId,
                    name = state.petName,
                    animal = "DOG",
                    breed = mapBreedToApiFormat(state.breed),
                    sex = apiGender,
                    birthday = formatBirthDateForApi(state.birthDate),
                    imgSrc = finalImageUrl, // 기존 또는 새 이미지 URL
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

    // 헬퍼 메서드들
    private fun mapApiBreedToDisplay(apiBreed: String): String {
        return when(apiBreed) {
            "POMERANIAN" -> "포메라니안"
            "POODLE" -> "푸들"
            "MALTESE" -> "말티즈"
            "SHIBA_INU" -> "시바견"
            "GOLDEN_RETRIEVER" -> "골든 리트리버"
            "CHIHUAHUA" -> "치와와"
            "BULLDOG" -> "불독"
            "BEAGLE" -> "비글"
            "MIXED" -> "믹스견"
            else -> "기타"
        }
    }

    private fun mapBreedToApiFormat(breed: String): String {
        return when(breed) {
            "포메라니안" -> "POMERANIAN"
            "푸들" -> "POODLE"
            "말티즈" -> "MALTESE"
            "시바견" -> "SHIBA_INU"
            "골든 리트리버" -> "GOLDEN_RETRIEVER"
            "치와와" -> "CHIHUAHUA"
            "불독" -> "BULLDOG"
            "비글" -> "BEAGLE"
            "믹스견" -> "MIXED"
            else -> "MIXED"
        }
    }

    private fun mapApiGenderToDisplay(apiGender: String): String {
        return when(apiGender) {
            "MALE" -> "남아"
            "FEMALE" -> "여아"
            else -> "남아"
        }
    }

    private fun formatApiDateToDisplay(apiDate: String): String {
        // "2025-08-12" -> "08/12/2025" 형식으로 변환
        if (apiDate.isBlank()) return ""

        val parts = apiDate.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            return "$month/$day/$year"
        }
        return apiDate
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