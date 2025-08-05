package com.example.petplace.data.remote

import com.example.petplace.data.model.feed.FeedRecommendRes
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedApiService {
    @GET("feeds/recommend")
    suspend fun getRecommendedFeeds(
        @Query("user_id") userId: Long = 0, //로그인된 유저 id (필수)
        @Query("page")    page: Int  = 0,
        @Query("size")    size: Int  = 20
    ): List<FeedRecommendRes>        // 200 OK → JSON Array
}
