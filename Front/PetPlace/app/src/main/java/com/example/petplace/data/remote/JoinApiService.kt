package com.example.petplace.data.remote

import com.example.petplace.data.model.join.CertificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JoinApiService {
    @GET("api/user/signup")
    suspend fun verifyCertification(
        @Query("imp_uid") impUid: String
    ): Response<CertificationResponse>
}