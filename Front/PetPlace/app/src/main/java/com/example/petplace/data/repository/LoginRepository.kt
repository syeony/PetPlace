package com.example.petplace.data.repository

import com.example.petplace.data.model.login.KakaoLoginRequest
import com.example.petplace.data.model.login.KakaoLoginResponse
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.remote.LoginApiService.TokenRefreshRequest
import com.example.petplace.data.remote.LoginApiService.TokenRefreshResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val api: LoginApiService
) {
    suspend fun login(id: String, pw: String): Result<String> {
        return try {
            val response = api.login(LoginApiService.LoginRequest(id, pw))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.accessToken)
            } else {
                Result.failure(Exception("로그인 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun refreshToken(refreshToken: TokenRefreshRequest) = api.refreshToken(refreshToken)
//    suspend fun isTokenValid() = api.isTokenValid()
    suspend fun isTokenValid(accessToken : String) = api.isTokenValid(accessToken)

    suspend fun refreshTokenBlocking(request: TokenRefreshRequest) = api.refreshTokenBlocking(request)

    suspend fun loginWithKakao(request: KakaoLoginRequest) =api.loginWithKakao(request)

}
