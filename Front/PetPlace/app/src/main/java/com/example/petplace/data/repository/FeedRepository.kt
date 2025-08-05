package com.example.petplace.data.repository

import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedCreateRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.remote.FeedApiService
import com.example.petplace.data.remote.LoginApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val api: FeedApiService
) {
    /** 서버-에러(401 등) → Exception 으로 던져서 ViewModel 에서 catch */
    suspend fun fetchRecommendedFeeds(
        userId: LoginApiService.User?,
        page: Int = 0,
        size: Int = 100
    ): List<FeedRecommendRes> = api.getRecommendedFeeds(userId, page, size)

    suspend fun createFeed(req: FeedCreateReq): FeedCreateRes {
        return api.createFeed(req)
    }

}
