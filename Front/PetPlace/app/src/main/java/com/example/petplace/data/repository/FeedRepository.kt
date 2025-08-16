package com.example.petplace.data.repository

import com.example.petplace.data.model.feed.CommentReq
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedCreateRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.LikeFeedReq
import com.example.petplace.data.model.feed.LikesRes
import com.example.petplace.data.remote.FeedApiService
import com.example.petplace.data.remote.LoginApiService
import retrofit2.HttpException
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

    suspend fun triggerBatch(): Result<Unit> = runCatching {
        val res = api.runRecommendBatch()
        if (res.isSuccessful) {
            Unit
        } else {
            throw HttpException(res)
        }
    }
    suspend fun fetchRecommendedFeeds2(
        page: Int = 0,
        size: Int = 100
    ): List<FeedRecommendRes> = api.getRecommendedFeeds2(page, size)

    suspend fun createFeed(req: FeedCreateReq): FeedCreateRes {
        return api.createFeed(req)
    }

    suspend fun likeFeed(feedId: Long): LikesRes {
        return api.likeFeed(LikeFeedReq(feedId))
    }
    suspend fun unlikeFeed(likeId: Long): LikesRes {
        return api.unlikeFeed(likeId)
    }

    suspend fun createComment(req: CommentReq): CommentRes {
        return api.createComment(req)
    }
    suspend fun deleteComment(commentId: Long): Long {
        return api.deleteComment(commentId).id
    }

    suspend fun getComments(feedId: Long): List<CommentRes> {
        return api.getComment(feedId)
    }

    suspend fun fetchComments(feedId: Long): List<CommentRes> {
        return api.getCommentsByFeedId(feedId)
    }

    // 피드 상세 조회
    suspend fun getFeedDetail(feedId: Long): FeedRecommendRes {
        return api.getFeedDetail(feedId)
    }

    // 피드 수정
    suspend fun editFeed(feedId: Long, req: FeedCreateReq): FeedRecommendRes {
        return api.editFeed(feedId, req)
    }

    suspend fun deleteFeed(feedId: Long): Long {
        return api.deleteFeed(feedId).id
    }
}
