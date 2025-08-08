package com.example.petplace.presentation.feature.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.repository.LoginRepository
import com.kakao.sdk.user.model.AccessTokenInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {
    private val app = PetPlaceApp.getAppContext() as PetPlaceApp

    /** AccessToken 유효성 체크 */
    suspend fun isTokenValid(accessToken : String): Boolean {
        return try {
            val response = repo.isTokenValid(accessToken) // 헤더 자동첨부됨
            response.isSuccessful?.let {
                Log.d("Token", "isTokenValid:${response.body()?.success} ")
            }
            response.isSuccessful && (response.body()?.success == true)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Refresh Token으로 갱신 시도 */
    suspend fun refreshToken(refreshToken: String): Boolean {
        return try {
            val resp = repo.refreshToken(LoginApiService.TokenRefreshRequest(refreshToken))
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                Log.d("Token", "refreshToken: 갱신됨 ")
                app.saveTokens(body.accessToken, body.refreshToken)
                true
            } else {
                Log.d("Token", "refreshToken: 갱신안댐 ")
                app.clearLoginData()
                false
            }
        } catch (e: Exception) {
            Log.e("Token", "refreshToken: 예외 발생", e)
            e.printStackTrace()
            false
        }
    }
}
