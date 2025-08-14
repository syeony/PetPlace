package com.example.petplace.data.repository

import com.example.petplace.data.model.join.CertificationResponse
import com.example.petplace.data.model.join.JoinRequest
import com.example.petplace.data.model.join.KakaoJoinRequest
import com.example.petplace.data.remote.JoinApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject

class JoinRepository @Inject constructor(
    private val api: JoinApiService
) {
    suspend fun prepareCertification() = api.prepareCertification()

    suspend fun verifyCertification(impUid: String) = api.verifyCertification(impUid)

    suspend fun checkUserName(userName: String)  = api.checkUser(userName)

    suspend fun checkNickname(nickname: String) = api.checkNickName(nickname)

    suspend fun signUp(signUp: JoinRequest) = api.signUp(signUp)
    suspend fun signUpKakao( request: KakaoJoinRequest) = api.signUpKakao(request)

    suspend fun verifyUserNeighborhood(lat: Double, lon: Double) = api.verifyUserNeighborhood(lat, lon)

}