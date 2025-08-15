package com.example.petplace.data.remote

import DongAuthResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @POST("/api/user/me/dong-authentication")
    suspend fun authenticateDong(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): DongAuthResponse
}
