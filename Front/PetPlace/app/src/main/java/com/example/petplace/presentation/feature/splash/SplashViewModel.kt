package com.example.petplace.presentation.feature.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repo: LoginRepository
) : ViewModel() {
    private val app = PetPlaceApp.getAppContext() as PetPlaceApp

    /**
     * RefreshToken으로 AccessToken 갱신 시도
     */
    suspend fun refreshToken(refreshToken: String): Boolean {
        return try {
            val refreshTokenResponse = repo.refreshToken(
                LoginApiService.TokenRefreshRequest(refreshToken)
            )

            if (refreshTokenResponse.isSuccessful && refreshTokenResponse.body() != null) {
                val refreshBody = refreshTokenResponse.body()!!
                // 토큰 저장
                app.saveTokens(refreshBody.accessToken, refreshBody.refreshToken)
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
