package com.example.petplace.data.repository

import android.content.Context
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.model.cares.CareUpdateRequest
import com.example.petplace.data.remote.CaresApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CaresRepository @Inject constructor(
    private val api: CaresApiService,
    @ApplicationContext private val context: Context
) {

    val app = context as PetPlaceApp
    val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    suspend fun list(
        page: Int = 0,
        size: Int = 20,
        regionId: Long,
        sort: String = "createdAt,desc"
    ) = runCatching { api.getCares(regionId, page, size, sort) }

    suspend fun myList(
        page: Int = 0,
        size: Int = 20,
        sort: String = "createdAt,desc"
    ) = runCatching { api.getMyCares(page, size, sort) }

//    suspend fun listContent(
//        page: Int = 0,
//        size: Int = 20,
//        regionId: Long? = null,
//        category: CareCategory? = null,
//        sort: String? = "createdAt,desc"
//    ): Result<List<CareItem>> = runCatching {
//        val resp = api.getCares(page, size, regionId, category, sort)
//        if (!resp.isSuccessful) error("HTTP ${resp.code()}: ${resp.errorBody()?.string()}")
//        val body = resp.body() ?: error("Empty body")
//        if (!body.success) error(body.message)
//        body.data?.content ?: emptyList()
//    }

    suspend fun detail(id: Long) =
        runCatching { api.getCareDetail(id) }

    suspend fun create(req: CareCreateRequest) =
        runCatching { api.createCare(req) }

    suspend fun update(id: Long, req: CareUpdateRequest) =
        runCatching { api.updateCare(id, req) }

    suspend fun delete(id: Long) =
        runCatching { api.deleteCare(id) }
}
