package com.example.petplace.data.remote

import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.model.cares.CareDetail
import com.example.petplace.data.model.cares.CareDetailDto
import com.example.petplace.data.model.cares.CareSummary
import com.example.petplace.data.model.cares.CareUpdateRequest
import com.example.petplace.data.model.cares.IdResponse
import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.presentation.feature.hotel.ApiResponse
import com.google.android.gms.common.api.Api
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.*

interface CaresApiService {
    // 목록 조회 (page/size는 스웨거에 맞게 조정)
    @GET("/api/cares")
    suspend fun getCares(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<List<CareSummary>>>

    // 상세
    @GET("/api/cares/{id}")
    suspend fun getCareDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<CareDetail>>

    // 생성
    @POST("/api/cares")
    suspend fun createCare(
        @Body req: CareCreateRequest
    ): Response<ApiResponse<IdResponse>>

    // 수정 (PUT 또는 PATCH — 스웨거에 맞추세요)
    @PUT("/api/cares/{id}")
    suspend fun updateCare(
        @Path("id") id: Long,
        @Body req: CareUpdateRequest
    ): Response<ApiResponse<Boolean>>

    // 삭제
    @DELETE("/api/cares/{id}")
    suspend fun deleteCare(
        @Path("id") id: Long
    ): Response<ApiResponse<Boolean>>
}
