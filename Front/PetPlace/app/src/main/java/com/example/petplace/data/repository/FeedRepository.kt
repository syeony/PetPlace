package com.example.petplace.data.repository

import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.remote.FeedApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val api: FeedApiService
) {
    /** 서버-에러(401 등) → Exception 으로 던져서 ViewModel 에서 catch */
    suspend fun fetchRecommendedFeeds(
        userId: Long = 0,
        page: Int = 0,
        size: Int = 20
    ): List<FeedRecommendRes> = api.getRecommendedFeeds(userId, page, size)
}
