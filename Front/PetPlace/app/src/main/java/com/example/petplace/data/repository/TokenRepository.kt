package com.example.petplace.data.repository

import com.example.petplace.data.model.fcm.FcmTokenRequest
import com.example.petplace.data.remote.PetApiService
import com.example.petplace.data.remote.TokenApiService
import javax.inject.Inject

class TokenRepository @Inject constructor(
    private val api: TokenApiService
){
        suspend fun registerFcmToken(req: FcmTokenRequest) = api.registerFcmToken(req)

        suspend fun deactivateFcmToken(req: String) = api.deactivateFcmToken(req)

}