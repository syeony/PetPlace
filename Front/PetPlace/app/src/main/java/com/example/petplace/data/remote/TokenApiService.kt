package com.example.petplace.data.remote

import com.example.petplace.data.model.fcm.FcmTokenRequest
import com.example.petplace.data.model.fcm.FcmTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

interface TokenApiService {
    @POST("/api/tokens")
    suspend fun registerFcmToken(
        @Body body: FcmTokenRequest
    ): Response<FcmTokenResponse>

    @DELETE("/api/tokens")
    suspend fun deactivateFcmToken(
        @Query("token") token: String
    ): Response<Unit> // 200 OK (body 없음)
}