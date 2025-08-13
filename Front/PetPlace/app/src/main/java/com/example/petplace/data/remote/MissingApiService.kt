package com.example.petplace.data.remote

import com.example.petplace.data.model.missing_list.MissingReportDto
import com.example.petplace.data.model.missing_list.PageResponse
import com.example.petplace.data.model.missing_register.CreateRegisterReq
import com.example.petplace.data.model.missing_register.RegisterRes
import com.example.petplace.data.model.missing_report.ApiResponse
import com.example.petplace.data.model.missing_report.CreateSightingReq
import com.example.petplace.data.model.missing_report.SightingRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MissingApiService {

    @POST("/api/missing/sightings")
    suspend fun createSighting(
        @Body req: CreateSightingReq
    ): ApiResponse<SightingRes>

    @POST("/api/missing/reports")
    suspend fun createRegister(
        @Body req: CreateRegisterReq
    ): ApiResponse<RegisterRes>

    @GET("/api/missing/reports")
    suspend fun getMissingReports(
        @Query("regionId") regionId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): ApiResponse<PageResponse<MissingReportDto>>
}