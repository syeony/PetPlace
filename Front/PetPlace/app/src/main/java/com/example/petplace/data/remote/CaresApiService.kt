package com.example.petplace.data.remote

import com.example.petplace.data.model.cares.CareDetailDto
import com.example.petplace.data.model.cares.CareUpdateRequest
import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.presentation.feature.hotel.ApiResponse
import com.google.android.gms.common.api.Api
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CaresApiService {

    @GET("/api/cares/{id}")
    suspend fun getCareDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<CareDetailDto>>

    @POST("/api/cares/{id}")
    suspend fun updateCareDetail(
        @Path("id") id: Long,
        @Body request: CareUpdateRequest
    ): Response<ApiResponse<CareDetailDto>>

    @DELETE("/api/cares/{id}")
    suspend fun deleteCareDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<String>>
    









}