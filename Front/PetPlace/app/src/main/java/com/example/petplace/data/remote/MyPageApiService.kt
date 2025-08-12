package com.example.petplace.data.remote

import com.example.petplace.data.model.mypage.MyPageInfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface MyPageApiService {

    @GET("/api/profiles/me")
    suspend fun getMyPageInfo(
    ): Response<MyPageInfoResponse>
}