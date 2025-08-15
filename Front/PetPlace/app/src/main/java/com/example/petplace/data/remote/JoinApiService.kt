package com.example.petplace.data.remote

import com.example.petplace.data.model.join.CertificationPrepareResponse
import com.example.petplace.data.model.join.CertificationResponse
import com.example.petplace.data.model.join.JoinRequest
import com.example.petplace.data.model.join.KakaoJoinRequest
import com.example.petplace.data.model.join.NeighborhoodResponse
import com.example.petplace.data.remote.LoginApiService.LoginResponse
import com.example.petplace.data.remote.LoginApiService.TokenRefreshRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.annotation.meta.TypeQualifierNickname

interface JoinApiService {
    @GET("api/user/signup")
    suspend fun verifyCertification(
        @Query("imp_uid") impUid: String
    ): Response<CertificationResponse>

    @POST("/api/user/check-username")
    suspend fun checkUser(@Query("user_name") userName: String): Response<CertificationPrepareResponse>

    @POST("/api/user/check-nickname")
    suspend fun checkNickName(@Query("nickname") nickname: String): Response<CertificationPrepareResponse>

    @POST("/api/user/signup")
    suspend fun signUp(@Body request: JoinRequest): Response<CertificationResponse>

    @POST("/api/user/certifications/prepare")
    suspend fun prepareCertification(): Response<CertificationPrepareResponse>

    @POST("/api/auth/social/signup")
    suspend fun signUpKakao(@Body request: KakaoJoinRequest): Response<LoginResponse>

    @POST("/api/user/me/dong-authentication")
    suspend fun verifyUserNeighborhood(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<NeighborhoodResponse>
}