package com.example.petplace.data.repository

import com.example.petplace.data.remote.JoinApiService
import javax.inject.Inject

class JoinRepository @Inject constructor(
    private val api: JoinApiService
) {
    suspend fun verifyCertification(impUid: String) = api.verifyCertification(impUid)
}