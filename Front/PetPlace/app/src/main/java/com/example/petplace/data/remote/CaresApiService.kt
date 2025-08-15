package com.example.petplace.data.remote

import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.model.cares.CareDetail
import com.example.petplace.data.model.cares.CareItem
import com.example.petplace.data.model.cares.CareUpdateRequest
import com.example.petplace.data.model.cares.IdResponse
import com.example.petplace.data.model.cares.PageResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.*

interface CaresApiService {
    @GET("/api/cares")
    suspend fun getCares(
        @Query("regionId") regionId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int? = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<PageResponse<CareItem>>>

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

    @GET("/api/cares/my")
    suspend fun getMyCares(
        @Query("page") page: Int = 0,
        @Query("size") size: Int? = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<PageResponse<CareItem>>>
}
