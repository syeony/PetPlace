package com.example.petplace.data.repository

import com.example.petplace.data.remote.LoginApiService
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val api: LoginApiService
) {
    suspend fun login(id: String, pw: String): Result<String> {
        return try {
            val response = api.login(LoginApiService.LoginRequest(id, pw))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.token)
            } else {
                Result.failure(Exception("로그인 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
