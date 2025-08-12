package com.example.petplace.data.repository

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
}