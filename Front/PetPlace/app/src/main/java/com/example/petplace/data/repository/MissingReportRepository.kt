package com.example.petplace.data.repository

import com.example.petplace.data.model.missing_list.MissingReportDto
import com.example.petplace.data.model.missing_list.PageResponse
import com.example.petplace.data.model.missing_register.CreateRegisterReq
import com.example.petplace.data.model.missing_register.RegisterRes
import com.example.petplace.data.model.missing_report.ApiResponse
import com.example.petplace.data.model.missing_report.CreateSightingReq
import com.example.petplace.data.model.missing_report.SightingRes
import com.example.petplace.data.remote.MissingApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissingSightingRepository @Inject constructor(
    private val api: MissingApiService
) {
    suspend fun createSighting(req: CreateSightingReq): Result<SightingRes> =
        runCatching {
            val res = api.createSighting(req)
            if (res.success && res.data != null) res.data
            else throw IllegalStateException(res.message ?: "등록에 실패했습니다.")
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
}