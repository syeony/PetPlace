package com.example.petplace.data.repository

import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.model.cares.CareUpdateRequest
import com.example.petplace.data.remote.CaresApiService
import javax.inject.Inject

class CaresRepository @Inject constructor(
    private val api: CaresApiService
) {
    suspend fun list(page: Int = 0, size: Int = 20) =
        runCatching { api.getCares(page, size) }

    suspend fun detail(id: Long) =
        runCatching { api.getCareDetail(id) }

    suspend fun create(req: CareCreateRequest) =
        runCatching { api.createCare(req) }

    suspend fun update(id: Long, req: CareUpdateRequest) =
        runCatching { api.updateCare(id, req) }

    suspend fun delete(id: Long) =
        runCatching { api.deleteCare(id) }
}
