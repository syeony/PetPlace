package com.example.petplace.presentation.feature.join

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petplace.data.model.join.CertificationPrepareResponse
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
class JoinViewModel @Inject constructor(
    private val repo: KakaoRepository,
    private val joinRepo: JoinRepository
) : ViewModel() {
    var userId = mutableStateOf("")
        private set
    var password = mutableStateOf("")
        private set
    var nickname = mutableStateOf("")
        private set

    var impUid = mutableStateOf<String?>("")
        private set


    fun onUserIdChange(newId: String) { userId.value = newId }
    fun onPasswordChange(newPw: String) { password.value = newPw }
    fun onNicknameChange(newName: String) { nickname.value = newName }
    fun saveImpUid(newImpUid: String) { impUid.value = newImpUid }

    // 동네 이름 상태 (null: 아직 로딩 전)
    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName

    fun fetchRegionByCoord(lat: Double, lng: Double) {
        viewModelScope.launch {
            _regionName.value = repo.getRegionByCoord(lat, lng)
        }
    }

    suspend fun prepareCertification(): CertificationPrepareResponse {
        // Retrofit Response<CertificationPrepareResponse> 반환을 가정
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
}
