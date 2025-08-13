package com.example.petplace.data.remote

import com.example.petplace.data.model.join.CertificationResponse
import com.example.petplace.data.model.login.KakaoLoginRequest
import com.example.petplace.data.model.login.KakaoLoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApiService {

    data class LoginRequest(
        val userName: String, // 서버에서 요구하는 필드명 확인
        val password: String
    )

    data class LoginResponse(
        val accessToken: String,
        val refreshToken: String,
        val message: String,
        val user: User
    )
    data class TokenRefreshRequest(
        val refreshToken : String
    )
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String,
    val success: Boolean
)

    data class User(
        val userId: Long,
        val userName: String,
        val nickname: String,
        val userImgSrc: String?,
        val level: Int,
        val defaultPetId: Int?,
        val regionId: Long,
        val phoneNumber: String
    )

    @POST("api/auth/login") // 실제 서버 경로로 수정
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ) : Response<TokenRefreshResponse>


    //  동기용 (Authenticator에서 사용)
    @POST("api/auth/refresh")
    fun refreshTokenBlocking(
        @Body request: TokenRefreshRequest
    ): Call<TokenRefreshResponse>

//    @GET("api/user/test-auth")
//    fun isTokenValid() : Response<CertificationResponse>

    @GET("api/auth/validate-token")
    fun isTokenValid(
        @Query("access_token") accessToken: String
    ) : Response<CertificationResponse>

    @POST("api/auth/social/login")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ) : Response<KakaoLoginResponse>


}
