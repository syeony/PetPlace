package com.example.petplace.data.remote

import com.example.petplace.data.model.missing_report.ApiResponse
import com.example.petplace.data.model.missing_report.CreateSightingReq
import com.example.petplace.data.model.missing_report.SightingRes
import retrofit2.http.Body
import retrofit2.http.POST

interface MissingApiService {

    @POST("/api/missing/sightings")
    suspend fun createSighting(
        @Body req: CreateSightingReq
    ): ApiResponse<SightingRes>

}