package com.example.petplace.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {

    // 🔹 요청 DTO
    data class LoginRequest(
        val userName: String, // 서버에서 요구하는 필드명 확인
        val password: String
    )

    // 🔹 응답 DTO
    data class LoginResponse(
        val accessToken: String,
        val refreshToken: String,
        val message: String,
        val user: User
    )

    data class User(
        val userName: String,
        val nickname: String,
        val userImgSrc: String?,
        val level: Int,
        val defaultPetId: Int?,
        val regionId: Long
    )
    @POST("api/auth/login") // 실제 서버 경로로 수정
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
