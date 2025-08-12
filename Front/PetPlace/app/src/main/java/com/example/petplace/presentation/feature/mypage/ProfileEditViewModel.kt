package com.example.petplace.presentation.feature.mypage

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.repository.MyPageRepository
import com.example.petplace.data.repository.ImageRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

data class ProfileEditUiState(
    val userName: String = "",
    val nickname: String = "",
    val introduction: String = "",
    val profileImageUri: Uri? = null,
    val profileImageUrl: String? = null,
    val isLocationVerified: Boolean = false,
    val currentLocation: String? = null,
    val isVerifyingLocation: Boolean = false,
//    val currentPassword: String = "",
//    val newPassword: String = "",
//    val confirmPassword: String = "",
//    val currentPwVisible: Boolean = false,
//    val newPwVisible: Boolean = false,
//    val confirmPwVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
//    val passwordValidationError: String? = null
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    init {
        initLocationClient()
        loadCurrentUserData()
    }

    private fun initLocationClient() {
        val context = PetPlaceApp.getAppContext()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        geocoder = Geocoder(context, Locale.KOREAN)
    }

    private fun loadCurrentUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                myPageRepository.getMyPageInfo()
                    .onSuccess { response ->
                        val app = PetPlaceApp.getAppContext() as PetPlaceApp
                        val userInfo = app.getUserInfo()

                        _uiState.value = _uiState.value.copy(
                            userName = userInfo?.userName ?: "", // 실제 userId 사용
                            nickname = response.nickname ?: "",
                            introduction = response.introduction ?: "",
                            profileImageUrl = response.userImgSrc, // 서버 이미지 URL
                            currentLocation = response.regionName,
                            isLocationVerified = !response.regionName.isNullOrEmpty(), // 지역 정보가 있으면 인증된 것으로 간주
                            isLoading = false
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "사용자 정보를 불러오는데 실패했습니다.",
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

    fun updateNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname)
    }

    fun updateIntroduction(introduction: String) {
        _uiState.value = _uiState.value.copy(introduction = introduction)
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }
    fun requestLocationVerification() {
//        if (_uiState.value.isLocationVerified) {
//            // 이미 인증된 경우 재인증 여부 확인
//            _uiState.value = _uiState.value.copy(
//                error = "이미 동네 인증이 완료되었습니다. 재인증하시겠습니까?"
//            )
//            return
//        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isVerifyingLocation = true,
                    error = null
                )

                val context = PetPlaceApp.getAppContext()

                // 위치 권한 확인
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingLocation = false,
                        error = "위치 권한이 필요합니다. 설정에서 위치 권한을 허용해주세요."
                    )
                    return@launch
                }

                // 현재 위치 가져오기
                val location = getCurrentLocation()

                if (location != null) {
                    // 실제 주소로 변환
                    val address = getAddressFromLocation(location.latitude, location.longitude)

                    if (address != null) {
                        // TODO: 서버에 위치 인증 정보 저장
                        // myPageRepository.updateLocationVerification(location.latitude, location.longitude, address)

                        _uiState.value = _uiState.value.copy(
                            isVerifyingLocation = false,
                            isLocationVerified = true,
                            currentLocation = address,
                            successMessage = "동네 인증이 완료되었습니다!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isVerifyingLocation = false,
                            error = "주소를 찾을 수 없습니다. 네트워크 연결을 확인하고 다시 시도해주세요."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingLocation = false,
                        error = "위치를 찾을 수 없습니다. GPS가 켜져있는지 확인하고 다시 시도해주세요."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingLocation = false,
                    error = "위치 인증 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val context = PetPlaceApp.getAppContext()

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMaxUpdateDelayMillis(5000)
                .setMinUpdateIntervalMillis(2000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(location)
                }
            }

            // 먼저 마지막 알려진 위치 시도
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // 최근 위치가 있으면 사용 (5분 이내)
                        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000
                        if (location.time > fiveMinutesAgo) {
                            continuation.resume(location)
                            return@addOnSuccessListener
                        }
                    }

                    // 최근 위치가 없거나 오래된 경우 새로 요청
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
                .addOnFailureListener {
                    // 마지막 위치 가져오기 실패 시 새로 요청
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }

            // 타임아웃 처리 (15초)
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    // 좌표를 실제 주소로 변환하는 함수
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val fullAddress = address.getAddressLine(0)

                        if (!fullAddress.isNullOrEmpty()) {
                            // "대한민국" 제거하고 반환
                            return@withContext fullAddress.replace("대한민국 ", "")
                        }
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }


//    fun toggleCurrentPasswordVisibility() {
//        _uiState.value = _uiState.value.copy(
//            currentPwVisible = !_uiState.value.currentPwVisible
//        )
//    }
//
//    fun toggleNewPasswordVisibility() {
//        _uiState.value = _uiState.value.copy(
//            newPwVisible = !_uiState.value.newPwVisible
//        )
//    }
//
//    fun toggleConfirmPasswordVisibility() {
//        _uiState.value = _uiState.value.copy(
//            confirmPwVisible = !_uiState.value.confirmPwVisible
//        )
//    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        if (state.nickname.trim().isEmpty()) {
            _uiState.value = state.copy(error = "닉네임을 입력해주세요.")
            return false
        }

        if (state.nickname.trim().length < 2) {
            _uiState.value = state.copy(error = "닉네임은 최소 2자 이상이어야 합니다.")
            return false
        }

        if (state.nickname.trim().length > 10) {
            _uiState.value = state.copy(error = "닉네임은 최대 10자까지 입력 가능합니다.")
            return false
        }

        return true
    }

    fun saveProfile(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                val currentState = _uiState.value

                var updateSuccessCount = 0
                val updateResults = mutableListOf<String>()

                // 1. 이미지 업데이트 처리
                currentState.profileImageUri?.let { uri ->
                    try {
                        Log.d("ProfileEdit", "이미지 업로드 시작")
                        val uploadedUrls = imageRepository.uploadImages(listOf(uri))

                        if (uploadedUrls.isNotEmpty()) {
                            val imageUrl = uploadedUrls.first()
                            Log.d("ProfileEdit", "이미지 업로드 완료: $imageUrl")

                            // 서버에 프로필 이미지 업데이트 요청
                            myPageRepository.updateProfileImage(imageUrl)
                                .onSuccess { response ->
                                    Log.d("ProfileEdit", "프로필 이미지 서버 업데이트 성공")
                                    _uiState.value = _uiState.value.copy(
                                        profileImageUrl = response.userImgSrc,
                                        profileImageUri = null // 업로드 완료 후 URI 초기화
                                    )
                                    updateSuccessCount++
                                    updateResults.add("프로필 이미지")
                                }
                                .onFailure { exception ->
                                    Log.e("ProfileEdit", "프로필 이미지 서버 업데이트 실패", exception)
                                    throw Exception("이미지 업데이트 실패: ${exception.message}")
                                }
                        } else {
                            throw Exception("이미지 업로드에 실패했습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileEdit", "이미지 처리 전체 실패", e)
                        throw e
                    }
                }

                // 2. 소개글 업데이트 처리
                val introductionText = currentState.introduction.trim()
                if (introductionText.isNotEmpty()) {
                    try {
                        Log.d("ProfileEdit", "소개글 업데이트 시작: '$introductionText'")

                        // 먼저 최신 프로필 정보를 가져와서 기존 소개글 여부 확인
                        myPageRepository.getMyPageInfo()
                            .onSuccess { myPageInfo ->
                                val hasExistingIntroduction = !myPageInfo.introduction.isNullOrBlank()
                                Log.d("ProfileEdit", "기존 소개글 존재 여부: $hasExistingIntroduction")

                                // 소개글 저장 (기존 여부에 따라 POST/PUT 결정)
                                launch {
                                    try {
                                        myPageRepository.saveProfileIntroduction(
                                            content = introductionText,
                                            isUpdate = hasExistingIntroduction
                                        )
                                            .onSuccess { introResponse ->
                                                Log.d("ProfileEdit", "소개글 저장 성공: ${introResponse.content}")
                                                updateSuccessCount++
                                                updateResults.add("소개글")
                                            }
                                            .onFailure { exception ->
                                                Log.e("ProfileEdit", "소개글 저장 실패", exception)

                                                // 만약 PUT이 실패했다면 POST로 재시도
                                                if (hasExistingIntroduction) {
                                                    Log.d("ProfileEdit", "PUT 실패로 POST 재시도")
                                                    myPageRepository.saveProfileIntroduction(
                                                        content = introductionText,
                                                        isUpdate = false
                                                    )
                                                        .onSuccess { introResponse ->
                                                            Log.d("ProfileEdit", "POST 재시도 성공: ${introResponse.content}")
                                                            updateSuccessCount++
                                                            updateResults.add("소개글")
                                                        }
                                                        .onFailure { retryException ->
                                                            Log.e("ProfileEdit", "POST 재시도도 실패", retryException)
                                                            throw Exception("소개글 저장 실패: ${retryException.message}")
                                                        }
                                                } else {
                                                    throw Exception("소개글 저장 실패: ${exception.message}")
                                                }
                                            }
                                    } catch (e: Exception) {
                                        Log.e("ProfileEdit", "소개글 저장 과정에서 예외", e)
                                        throw e
                                    }
                                }
                            }
                            .onFailure { exception ->
                                Log.e("ProfileEdit", "프로필 정보 조회 실패", exception)
                                // 프로필 정보 조회 실패 시 기본적으로 업데이트로 시도
                                myPageRepository.saveProfileIntroduction(
                                    content = introductionText,
                                    isUpdate = true
                                )
                                    .onSuccess { introResponse ->
                                        Log.d("ProfileEdit", "기본 업데이트로 소개글 저장 성공")
                                        updateSuccessCount++
                                        updateResults.add("소개글")
                                    }
                                    .onFailure { introException ->
                                        Log.e("ProfileEdit", "기본 업데이트로도 실패", introException)
                                        throw Exception("소개글 저장 실패: ${introException.message}")
                                    }
                            }

                    } catch (e: Exception) {
                        Log.e("ProfileEdit", "소개글 처리 중 오류", e)
                        throw e
                    }
                }

                // 작업 완료 대기 (비동기 작업들이 완료될 때까지)
                delay(1000)

                // 3. 결과 처리
                val successMessage = when {
                    updateSuccessCount == 0 -> "변경사항이 저장되지 않았습니다."
                    updateResults.size == 1 -> "${updateResults[0]}가 업데이트되었습니다."
                    else -> "${updateResults.joinToString(", ")}가 업데이트되었습니다."
                }

                Log.d("ProfileEdit", "전체 업데이트 완료: $successMessage")

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = successMessage
                )

                onSuccess()

            } catch (e: Exception) {
                Log.e("ProfileEdit", "프로필 저장 실패", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "프로필 업데이트 중 오류가 발생했습니다."
                )
            }
        }
    }

//    private fun validatePasswordChange(): Boolean {
//        val state = _uiState.value
//
//        if (state.newPassword.isNotEmpty() || state.confirmPassword.isNotEmpty()) {
//            if (state.currentPassword.isEmpty()) {
//                _uiState.value = state.copy(
//                    passwordValidationError = "현재 비밀번호를 입력해주세요."
//                )
//                return false
//            }
//
//            if (state.newPassword.isEmpty()) {
//                _uiState.value = state.copy(
//                    passwordValidationError = "새 비밀번호를 입력해주세요."
//                )
//                return false
//            }
//
//            if (state.newPassword != state.confirmPassword) {
//                _uiState.value = state.copy(
//                    passwordValidationError = "새 비밀번호가 일치하지 않습니다."
//                )
//                return false
//            }
//
//            if (state.newPassword.length < 6) {
//                _uiState.value = state.copy(
//                    passwordValidationError = "비밀번호는 최소 6자 이상이어야 합니다."
//                )
//                return false
//            }
//        }
//
//        return true
//    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null,
//            passwordValidationError = null
        )
    }
}