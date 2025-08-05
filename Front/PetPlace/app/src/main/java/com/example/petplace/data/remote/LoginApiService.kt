package com.example.petplace.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {

    // 🔹 요청 DTO
    data class LoginRequest(
        val username: String, // 서버에서 요구하는 필드명 확인
        val password: String
    )

    // 🔹 응답 DTO
    data class LoginResponse(
        val token: String // 서버 JWT 응답 키에 맞게 수정 (예: "accessToken"이면 이름 변경)
    )

    @POST("api/auth/login") // 실제 서버 경로로 수정
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
