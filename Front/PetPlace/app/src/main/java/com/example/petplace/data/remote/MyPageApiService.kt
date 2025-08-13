package com.example.petplace.data.remote

import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.model.mypage.PetProductRequest
import com.example.petplace.data.model.mypage.PetProductResponse
import com.example.petplace.data.model.mypage.ProfileImageRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionResponse
import com.example.petplace.data.model.mypage.ProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface MyPageApiService {

    @GET("/api/profiles/me")
    suspend fun getMyPageInfo(
    ): Response<MyPageInfoResponse>

    @PUT("/api/profiles/me/user_image")
    suspend fun updatePetProductImage(
        @Body request: PetProductRequest
    ): Response<PetProductResponse>

    @POST("/api/profiles/me/user_image")
    suspend fun addPetProductImage(
        @Body request: PetProductRequest
    ): Response<PetProductResponse>

    @PUT("/api/profiles/me/profile_image")
    suspend fun updateProfileImage(
        @Body request: ProfileImageRequest
    ): Response<MyPageInfoResponse>

    @POST("/api/profiles")
    suspend fun createProfileIntroduction(
        @Body request: ProfileIntroductionRequest
    ): Response<ProfileIntroductionResponse>

    @PUT("/api/profiles/me")
    suspend fun updateProfileIntroduction(
        @Body request: ProfileIntroductionRequest
    ): Response<ProfileIntroductionResponse>

    @PUT("/api/profiles/me/update")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<MyPageInfoResponse>
}