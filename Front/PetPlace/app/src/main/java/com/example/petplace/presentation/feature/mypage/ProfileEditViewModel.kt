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
import com.example.petplace.data.repository.JoinRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
import kotlin.math.absoluteValue

data class ProfileEditUiState(
    val userName: String = "",
    val nickname: String = "",
    val introduction: String = "",
    val profileImageUri: Uri? = null,
    val profileImageUrl: String? = null,
    val isLocationVerified: Boolean = false,
    val currentLocation: String? = null,
    val currentRegionId: Long? = null,
    val isVerifyingLocation: Boolean = false,
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
    private val myPageRepository: MyPageRepository,
    private val imageRepository: ImageRepository,
    private val joinRepository: JoinRepository,
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

                myPageRepository.getMyPageInfo().onSuccess { response ->
                        val app = PetPlaceApp.getAppContext() as PetPlaceApp
                        val userInfo = app.getUserInfo()

                        _uiState.value = _uiState.value.copy(
                            userName = userInfo?.userName ?: "", // 실제 userId 사용
                            nickname = response.nickname ?: "",
                            introduction = response.introduction ?: "",
                            profileImageUrl = response.userImgSrc, // 서버 이미지 URL
                            profileImageUri = null,
                            currentLocation = response.regionName,
                            isLocationVerified = !response.regionName.isNullOrEmpty(), // 지역 정보가 있으면 인증된 것으로 간주
                            isLoading = false
                        )
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "사용자 정보를 불러오는데 실패했습니다.", isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message, isLoading = false
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

    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password, passwordValidationError = null // 입력할 때 오류 메시지 클리어
        )

        // 실시간 유효성 검증 (비밀번호를 입력하고 있을 때)
        if (password.isNotEmpty()) {
            validatePasswordRealtime()
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password, passwordValidationError = null // 입력할 때 오류 메시지 클리어
        )

        // 실시간 유효성 검증 (확인 비밀번호를 입력하고 있을 때)
        if (password.isNotEmpty()) {
            validatePasswordRealtime()
        }
    }

    fun updateCurrentPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password, passwordValidationError = null // 입력할 때 오류 메시지 클리어
        )
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

    fun requestLocationVerification() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isVerifyingLocation = true, error = null
                )

                val context = PetPlaceApp.getAppContext()

                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingLocation = false, error = "위치 권한이 필요합니다. 설정에서 위치 권한을 허용해주세요."
                    )
                    return@launch
                }

                val location = getCurrentLocation()

                if (location != null) {
                    Log.d("location", "requestLocationVerification: ${location}")
                    val addressDeferred =
                        async { getAddressFromLocation(location.latitude, location.longitude) }
                    val regionIdDeferred =
                        async { getRegionIdFromLocation(location.latitude, location.longitude) }

                    val address = addressDeferred.await()
                    val regionId = regionIdDeferred.await()

                    if (address != null && regionId != null) {
                        Log.d("ProfileEdit", "위치 인증 성공")
                        Log.d("ProfileEdit", "주소: $address")
                        Log.d("ProfileEdit", "지역코드: $regionId")

                        _uiState.value = _uiState.value.copy(
                            isVerifyingLocation = false,
                            isLocationVerified = true,
                            currentLocation = address,
                            currentRegionId = regionId,
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
                Log.e("ProfileEdit", "위치 인증 중 오류", e)
                _uiState.value = _uiState.value.copy(
                    isVerifyingLocation = false, error = "위치 인증 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }

    private suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                val context = PetPlaceApp.getAppContext()

                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val locationRequest =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setMaxUpdateDelayMillis(5000).setMinUpdateIntervalMillis(2000).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(location)
                    }
                }

                // 먼저 마지막 알려진 위치 시도
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
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
                            locationRequest, locationCallback, null
                        )
                    }.addOnFailureListener {
                        // 마지막 위치 가져오기 실패 시 새로 요청
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest, locationCallback, null
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

        if (!validatePasswordChange()) {
            return false
        }

        return true
    }

    private fun validatePasswordRealtime() {
        val state = _uiState.value

        // 새 비밀번호가 입력된 경우에만 검증
        if (state.newPassword.isNotEmpty()) {
            when {
                state.newPassword.length < 6 -> {
                    _uiState.value = state.copy(
                        passwordValidationError = "비밀번호는 최소 6자 이상이어야 합니다."
                    )
                    return
                }

                state.newPassword == state.currentPassword && state.currentPassword.isNotEmpty() -> {
                    _uiState.value = state.copy(
                        passwordValidationError = "새 비밀번호는 현재 비밀번호와 달라야 합니다."
                    )
                    return
                }
            }
        }

        // 확인 비밀번호가 입력된 경우 일치 여부 검증
        if (state.confirmPassword.isNotEmpty() && state.newPassword.isNotEmpty()) {
            if (state.newPassword != state.confirmPassword) {
                _uiState.value = state.copy(
                    passwordValidationError = "새 비밀번호가 일치하지 않습니다."
                )
                return
            }
        }

        // 모든 검증을 통과한 경우 오류 메시지 클리어
        _uiState.value = state.copy(passwordValidationError = null)
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

            // 새 비밀번호와 현재 비밀번호가 같은지 검증
            if (state.newPassword == state.currentPassword) {
                _uiState.value = state.copy(
                    passwordValidationError = "새 비밀번호는 현재 비밀번호와 달라야 합니다."
                )
                return false
            }
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

                // 1. 이미지 업로드 처리 (필요한 경우)
                var uploadedImageUrl: String? = null
                currentState.profileImageUri?.let { uri ->
                    try {
                        Log.d("ProfileEdit", "이미지 업로드 시작")
                        val uploadedUrls = imageRepository.uploadImages(listOf(uri))

                        if (uploadedUrls.isNotEmpty()) {
                            uploadedImageUrl = uploadedUrls.first()
                            Log.d("ProfileEdit", "이미지 업로드 완료: $uploadedImageUrl")
                        } else {
                            throw Exception("이미지 업로드에 실패했습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileEdit", "이미지 업로드 실패", e)
                        throw e
                    }
                }

                // 2. 프로필 통합 업데이트 API 호출
                val hasPasswordChange =
                    currentState.newPassword.isNotEmpty() && currentState.currentPassword.isNotEmpty()
                val hasNicknameChange = currentState.nickname.trim() != currentState.userName
                val hasImageChange = uploadedImageUrl != null
                val hasLocationChange =
                    currentState.isLocationVerified && currentState.currentRegionId != null

                Log.d(
                    "ProfileEdit", """
                        업데이트 체크:
                        - 비밀번호 변경: $hasPasswordChange
                        - 닉네임 변경: $hasNicknameChange  
                        - 이미지 변경: $hasImageChange
                        - 현재 비밀번호: ${if (currentState.currentPassword.isNotEmpty()) "입력됨" else "비어있음"}
                        - 새 비밀번호: ${if (currentState.newPassword.isNotEmpty()) "입력됨" else "비어있음"}
                        - regionId: ${currentState.currentRegionId}
                    """.trimIndent()
                )

                // 프로필 업데이트가 필요한 경우에만 API 호출
                if (hasPasswordChange || hasNicknameChange || hasImageChange) {
                    try {
                        myPageRepository.updateProfile(
                            nickname = if (hasNicknameChange) currentState.nickname.trim() else null,
                            curPassword = if (hasPasswordChange) currentState.currentPassword else null,
                            newPassword = if (hasPasswordChange) currentState.newPassword else null,
                            imgSrc = uploadedImageUrl,
                            regionId = currentState.currentRegionId
                        )
                            .onSuccess { response ->
                                Log.d("ProfileEdit", "프로필 통합 업데이트 성공")
                                // 기존 성공 처리 로직...
                            }
                            .onFailure { exception ->
                                Log.e("ProfileEdit", "프로필 통합 업데이트 실패", exception)
                                throw exception // 예외를 다시 던져서 catch 블록에서 처리
                            }
                    } catch (e: Exception) {
                        Log.e("ProfileEdit", "프로필 업데이트 처리 중 예외", e)

                        // 디버깅용 로그 추가
                        Log.d("ProfileEdit", "예외 메시지: ${e.message}")
                        Log.d("ProfileEdit", "currentRegionId: ${currentState.currentRegionId}")
                        Log.d("ProfileEdit", "500 에러 포함 여부: ${e.message?.contains("500")}")
                        Log.d("ProfileEdit", "서버 에러 포함 여부: ${e.message?.contains("서버")}")

                        // 조건을 더 넓게 수정
                        val is500Error = e.message?.contains("500") == true ||
                                e.message?.contains("서버에서 요청을 처리하는 중 오류가 발생했습니다") == true
                        val hasRegionId = currentState.currentRegionId != null

                        Log.d("ProfileEdit", "재시도 조건 - is500Error: $is500Error, hasRegionId: $hasRegionId")

                        if (is500Error && hasRegionId) {
                            Log.d("ProfileEdit", "지역 코드 오류로 인한 재시도 - 기본 코드 사용")

                            try {
                                // 기본 인동동 코드로 재시도
                                myPageRepository.updateProfile(
                                    nickname = if (hasNicknameChange) currentState.nickname.trim() else null,
                                    curPassword = if (hasPasswordChange) currentState.currentPassword else null,
                                    newPassword = if (hasPasswordChange) currentState.newPassword else null,
                                    imgSrc = uploadedImageUrl,
                                    regionId = 37050700L // 인동동 기본 코드
                                )
                                    .onSuccess { response ->
                                        Log.d("ProfileEdit", "기본 지역 코드로 재시도 성공")

                                        // UI 상태 업데이트
                                        _uiState.value = _uiState.value.copy(
                                            profileImageUrl = response.userImgSrc,
                                            profileImageUri = null,
                                            nickname = response.nickname ?: currentState.nickname,
                                            currentRegionId = 37050700L // 기본 코드로 업데이트
                                        )

                                        // 성공한 항목들 추적
                                        if (hasImageChange) updateResults.add("프로필 이미지")
                                        if (hasNicknameChange) updateResults.add("닉네임")
                                        if (hasPasswordChange) {
                                            updateResults.add("비밀번호")
                                            Log.d("ProfileEdit", "비밀번호 변경 성공 (재시도)")
                                            // 비밀번호 필드 초기화
                                            _uiState.value = _uiState.value.copy(
                                                currentPassword = "",
                                                newPassword = "",
                                                confirmPassword = ""
                                            )
                                        }
                                    }
                                    .onFailure { retryException ->
                                        Log.e("ProfileEdit", "기본 코드 재시도도 실패", retryException)
                                        throw retryException
                                    }
                            } catch (retryE: Exception) {
                                Log.e("ProfileEdit", "재시도 중 예외 발생", retryE)
                                throw retryE
                            }
                        } else {
                            Log.d("ProfileEdit", "재시도 조건에 맞지 않음 - 원래 예외 던지기")
                            throw e
                        }
                    }
                } else {
                    Log.d("ProfileEdit", "프로필 업데이트 항목이 없음")
                }

                // 3. 소개글 업데이트 처리 (별도 API)
                val introductionText = currentState.introduction.trim()
                if (introductionText.isNotEmpty()) {
                    try {
                        Log.d("ProfileEdit", "소개글 업데이트 시작: '$introductionText'")

                        // 먼저 최신 프로필 정보를 가져와서 기존 소개글 여부 확인
                        myPageRepository.getMyPageInfo().onSuccess { myPageInfo ->
                                val hasExistingIntroduction =
                                    !myPageInfo.introduction.isNullOrBlank()
                                Log.d("ProfileEdit", "기존 소개글 존재 여부: $hasExistingIntroduction")

                                // 소개글 저장 (기존 여부에 따라 POST/PUT 결정)
                                launch {
                                    try {
                                        myPageRepository.saveProfileIntroduction(
                                            content = introductionText,
                                            isUpdate = hasExistingIntroduction
                                        ).onSuccess { introResponse ->
                                                Log.d(
                                                    "ProfileEdit",
                                                    "소개글 저장 성공: ${introResponse.content}"
                                                )
                                                updateResults.add("소개글")
                                                updateSuccessCount++
                                            }.onFailure { exception ->
                                                Log.e("ProfileEdit", "소개글 저장 실패", exception)

                                                // 만약 PUT이 실패했다면 POST로 재시도
                                                if (hasExistingIntroduction) {
                                                    Log.d("ProfileEdit", "PUT 실패로 POST 재시도")
                                                    myPageRepository.saveProfileIntroduction(
                                                        content = introductionText, isUpdate = false
                                                    ).onSuccess { introResponse ->
                                                            Log.d(
                                                                "ProfileEdit",
                                                                "POST 재시도 성공: ${introResponse.content}"
                                                            )
                                                            updateResults.add("소개글")
                                                            updateSuccessCount++
                                                        }.onFailure { retryException ->
                                                            Log.e(
                                                                "ProfileEdit",
                                                                "POST 재시도도 실패",
                                                                retryException
                                                            )
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
                            }.onFailure { exception ->
                                Log.e("ProfileEdit", "프로필 정보 조회 실패", exception)
                                // 프로필 정보 조회 실패 시 기본적으로 업데이트로 시도
                                myPageRepository.saveProfileIntroduction(
                                    content = introductionText, isUpdate = true
                                ).onSuccess { introResponse ->
                                        Log.d("ProfileEdit", "기본 업데이트로 소개글 저장 성공")
                                        updateResults.add("소개글")
                                        updateSuccessCount++
                                    }.onFailure { introException ->
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

                // 4. 결과 처리
                val successMessage = when {
                    updateResults.isEmpty() -> "변경사항이 저장되지 않았습니다."
                    updateResults.size == 1 -> "${updateResults[0]}가 업데이트되었습니다."
                    else -> "${updateResults.joinToString(", ")}가 업데이트되었습니다."
                }

                Log.d("ProfileEdit", "전체 업데이트 완료: $successMessage")

                _uiState.value = _uiState.value.copy(
                    isSaving = false, successMessage = successMessage
                )

                onSuccess()

            } catch (e: Exception) {
                Log.e("ProfileEdit", "프로필 저장 실패", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false, error = e.message ?: "프로필 업데이트 중 오류가 발생했습니다."
                )
            }
        }
    }

    private suspend fun getRegionIdFromLocation(latitude: Double, longitude: Double): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val response = joinRepository.verifyUserNeighborhood(
                    lat = latitude,
                    lon = longitude
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        return@withContext body.data.regionId
                    } else {
                        Log.e("ProfileEdit", "동네 인증 실패: ${body?.message}")
                    }
                } else {
                    Log.e("ProfileEdit", "API 호출 실패: ${response.code()} ${response.message()}")
                }

                return@withContext null
            } catch (e: Exception) {
                Log.e("ProfileEdit", "동네 인증 중 오류 발생", e)
                return@withContext null
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null, successMessage = null, passwordValidationError = null
        )
    }
}