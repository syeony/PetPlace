package com.example.petplace.presentation.feature.join

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.join.CertificationPrepareResponse
import com.example.petplace.data.model.join.KakaoJoinRequest
import com.example.petplace.data.repository.JoinRepository
import com.example.petplace.data.repository.KakaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class KakaoJoinViewModel @Inject constructor(
    private val repo: KakaoRepository,
    private val joinRepo: JoinRepository,
) : ViewModel() {

    var tempToken = mutableStateOf("")
        private set

    var nickname = mutableStateOf("")
        private set

    var impUid = mutableStateOf<String?>(null)
        private set

    var nicknameChecked = mutableStateOf(false)
        private set

    fun setTempToken(newTempToken: String){ tempToken.value = newTempToken }
    fun onNicknameChange(newName: String) { nickname.value = newName }
    fun saveImpUid(newImpUid: String) { impUid.value = newImpUid }

    // --- 지역 정보 상태 ---
    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName

    // regionId도 상태로 보관 (서버 스키마가 Long일 가능성 높음)
    private val _regionId = MutableStateFlow<Long?>(null)
    val regionId: StateFlow<Long?> = _regionId


    fun fetchRegionByCoord(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // 1) 서버에 동네 인증 요청(성공 시 regionId 수신)
                val response = joinRepo.verifyUserNeighborhood(lat, lng)
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        // 서버가 주는 값 타입이 Int면 Long으로 승격
                        _regionId.value = when (val id = data.regionId) {
                            is Long -> id
                            is Int -> id.toLong()
                            is String -> id.toLongOrNull()
                            else -> null
                        }
                        Log.d("JoinViewModel", "동네 인증 성공: regionId=${_regionId.value}")
                    } ?: run {
                        Log.e("JoinViewModel", "동네 인증 응답 바디에 data 없음")
                    }
                } else {
                    Log.e(
                        "JoinViewModel",
                        "동네 인증 실패: ${response.code()} ${response.message()}"
                    )
                }

                // 2) 지역 이름은 별도로 역지오코딩
                _regionName.value = repo.getRegionByCoord(lat, lng)
            } catch (e: Exception) {
                Log.e("JoinViewModel", "fetchRegionByCoord 예외", e)
            }
        }
    }

    suspend fun checkNickname(): Boolean {
        return try {
            val response = joinRepo.checkNickname(nickname.value)
            val success = response.body()?.success == true
            nicknameChecked.value = success
            success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun prepareCertification(): CertificationPrepareResponse {
        val resp = withContext(Dispatchers.IO) {
            joinRepo.prepareCertification()
        }
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null && body.success) {
                return body
            } else {
                throw RuntimeException("prepareCertification 실패: ${body?.message ?: "응답 바디 없음"}")
            }
        } else {
            throw RuntimeException("prepareCertification HTTP 오류: ${resp.code()} / ${resp.errorBody()?.string()}")
        }
    }

    fun verifyCertification(impUid: String) {
        viewModelScope.launch {
            try {
                val response = joinRepo.verifyCertification(impUid)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("Certification", "인증 성공: ${response.body()}")
                } else {
                    Log.e("Certification", "인증 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Certification", "예외 발생", e)
            }
        }
    }

    suspend fun kakaoSignUp(): Boolean {
        return try {
            val currentRegionId = _regionId.value
            if (currentRegionId == null) {
                Log.e("KakaoJoin", "회원가입 실패: regionId가 설정되지 않음. fetchRegionByCoord를 먼저 호출하세요.")
                return false
            }
            val currentImpUid = impUid.value
            if (currentImpUid.isNullOrBlank()) {
                Log.e("KakaoJoin", "회원가입 실패: impUid가 없음. 본인인증 먼저 완료하세요.")
                return false
            }

            val request = KakaoJoinRequest(
                provider = "KAKAO",
                tempToken = tempToken.value,
                impUid = currentImpUid,
                nickname = nickname.value,
                // 하드코딩 제거: 서버 검증으로 얻은 regionId 사용
                regionId = currentRegionId, // ← KakaoJoinRequest가 Long을 받도록 정의되어 있어야 함
            )
            Log.d("KakaoJoin", "signUp 요청: tempToken=${tempToken.value}, regionId=$currentRegionId")
            val response = joinRepo.signUpKakao(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("KakaoJoin", "회원가입 실패", e)
            false
        }
    }
}
