package com.example.petplace.data.repository

import com.example.petplace.data.model.missing_list.MissingReportDto
import com.example.petplace.data.model.missing_list.PageResponse
import com.example.petplace.data.model.missing_register.CreateRegisterReq
import com.example.petplace.data.model.missing_register.RegisterRes
import com.example.petplace.data.model.missing_report.ApiResponse
import com.example.petplace.data.model.missing_report.MissingReportDetailDto
import com.example.petplace.data.model.missing_report.SightingRequest
import com.example.petplace.data.model.missing_report.SightingResponse
import com.example.petplace.data.remote.MissingApiService
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissingSightingRepository @Inject constructor(
    private val api: MissingApiService
) {
//    suspend fun createSighting(req: SightingRequest): Result<SightingResponse> =
//        runCatching {
//            val res = api.createSighting(req)
//            if (res.success && res.data != null) res.data
//            else throw IllegalStateException(res.message ?: "등록에 실패했습니다.")
//        }
suspend fun createSighting(req: SightingRequest): Result<SightingResponse> = runCatching {
    val res = api.createSighting(req)
    if (!res.isSuccessful) throw retrofit2.HttpException(res)

    val body = res.body() ?: error("Empty body")
    if (!body.success) error(body.message ?: "Server returned success=false")

    body.data!! // <- 최종 데이터만 반환
}
    suspend fun createRegister(req: CreateRegisterReq): Result<RegisterRes> =
        runCatching {
            val res = api.createRegister(req)
            if (res.success && res.data != null) res.data
            else throw IllegalStateException(res.message ?: "등록에 실패했습니다.")
        }

    suspend fun fetchMissingReports(
        regionId: Long,
        page: Int = 0,
        size: Int = 20,
        sort: String = "createdAt,desc"
    ): ApiResponse<PageResponse<MissingReportDto>> {
        return api.getMissingReports(regionId, page, size, sort)
    }
    suspend fun getMissingReportDetail(id: Long): Result<MissingReportDetailDto> = runCatching {
        val res = api.getMissingReportDetail(id)

        if (!res.isSuccessful) throw retrofit2.HttpException(res)

        val body = res.body() ?: error("Empty body")
        if (body.success != true) error(body.message ?: "Server returned success=false")

        body.data ?: error("Empty data")
    }


}