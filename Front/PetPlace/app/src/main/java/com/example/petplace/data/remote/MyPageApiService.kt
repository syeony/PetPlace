package com.example.petplace.data.remote

import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.model.mypage.PetProductRequest
import com.example.petplace.data.model.mypage.PetProductResponse
import com.example.petplace.data.model.mypage.ProfileImageRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionResponse
import com.example.petplace.data.model.mypage.ProfileUpdateRequest
import com.example.petplace.data.model.mypage.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @GET("/api/feeds/me")
    suspend fun getMyPosts(
    ): Response<List<FeedRecommendRes>>

    @GET("/api/comments")
    suspend fun getMyComments(
    ): Response<List<CommentRes>>

    @GET("/api/likes/me")
    suspend fun getMyLikePosts(
    ): Response<List<FeedRecommendRes>>

    @GET("/api/profiles/{id}")
    suspend fun getUserProfile(
        @Path("id") id: Long
    ): Response<UserProfileResponse>

}