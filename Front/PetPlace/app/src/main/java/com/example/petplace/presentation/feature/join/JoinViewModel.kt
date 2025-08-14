package com.example.petplace.presentation.feature.join

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.join.CertificationPrepareResponse
import com.example.petplace.data.model.join.JoinRequest
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

    var impUid = mutableStateOf<String?>(null)
        private set
    var userNameChecked = mutableStateOf(false)
        private set
    var nicknameChecked = mutableStateOf(false)
        private set

    private val _regionId = MutableStateFlow<Long>(37050700L)
    val regionId: StateFlow<Long> = _regionId

    fun onUserIdChange(newId: String) { userId.value = newId }
    fun onPasswordChange(newPw: String) { password.value = newPw }
    fun onNicknameChange(newName: String) { nickname.value = newName }
    fun saveImpUid(newImpUid: String) { impUid.value = newImpUid }

    suspend fun checkUserName(): Boolean {
        return try {
            val response = joinRepo.checkUserName(userId.value)
            val success = response.body()?.success == true
            userNameChecked.value = success
            success
        } catch (e: Exception) {
            false
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

    // 동네 이름 상태
    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName

    fun fetchRegionByCoord(lat: Double, lng: Double) {
        viewModelScope.launch {
            val response = joinRepo.verifyUserNeighborhood(lat, lng)
            if (response.isSuccessful) {
                response.body()?.data?.let { data ->
                    _regionId.value = data.regionId
                }
            } else {
                Log.e("JoinViewModel", "동네 인증 실패: ${response.code()} ${response.message()}")
            }
            _regionName.value = repo.getRegionByCoord(lat, lng)
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

    suspend fun signUp(): Boolean {
        return try {
            val request = JoinRequest(userId.value,password.value,nickname.value,regionId.value,impUid.value!!)
            val response = joinRepo.signUp(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
