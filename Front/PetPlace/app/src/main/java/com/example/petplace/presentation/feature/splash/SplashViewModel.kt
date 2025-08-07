package com.example.petplace.presentation.feature.splash

import androidx.lifecycle.ViewModel
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {
    private val app = PetPlaceApp.getAppContext() as PetPlaceApp

    /** AccessToken 유효성 체크 */
    suspend fun isTokenValid(): Boolean {
        return try {
            val response = repo.isTokenValid() // 헤더 자동첨부됨
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
                app.saveTokens(body.accessToken, body.refreshToken)
                true
            } else {
                app.clearLoginData()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
