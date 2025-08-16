package com.example.petplace.data.remote

import com.example.petplace.data.model.feed.CommentReq
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.DeleteCommentRes
import com.example.petplace.data.model.feed.DeleteFeedRes
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedCreateRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.LikeFeedReq
import com.example.petplace.data.model.feed.LikesRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface FeedApiService {
    //사용자 기반 추천 피드들
    @GET("api/feeds/recommend")
    suspend fun getRecommendedFeeds(
        @Query("user_id") userId: LoginApiService.User?, //로그인된 유저 id (필수)
        @Query("page") page: Int  = 0,
        @Query("size") size: Int  = 100
    ): List<FeedRecommendRes>

    @POST("api/recommend/batch")
    suspend fun runRecommendBatch(): Response<Unit>

    @GET("api/recommend/group")
    suspend fun getRecommendedFeeds2(
        @Query("page") page: Int  = 0,
        @Query("size") size: Int  = 100
    ): List<FeedRecommendRes>

    //피드 등록
    @POST("/api/feeds")
    suspend fun createFeed(
        @Body req: FeedCreateReq
    ): FeedCreateRes

    //좋아요 등록
    @POST("api/likes")
    suspend fun likeFeed(
        @Body body: LikeFeedReq
    ): LikesRes

    //좋아요 취소
    @DELETE("api/likes/{id}")
    suspend fun unlikeFeed(
        @Path("id") likeId: Long
    ): LikesRes

    //댓글 등록
    @POST("/api/comments")
    suspend fun createComment(
        @Body req: CommentReq
    ): CommentRes

    //댓글 삭제
    @DELETE("/api/comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: Long
    ): DeleteCommentRes

    //댓글 단건 조회
    @GET("/api/comments/{feed_id}")
    suspend fun getComment(
        @Path("feed_id") feedId: Long
    ): List<CommentRes>

    //피드id로 피드댓글목록조회
    @GET("/api/comments/feed_id")
    suspend fun getCommentsByFeedId(
        @Query("feed_id") feedId: Long
    ): List<CommentRes>

    // 피드 상세 조회 (수정시 기존 데이터 불러오기용)
    @GET("/api/feeds/{id}")
    suspend fun getFeedDetail(@Path("id") feedId: Long): FeedRecommendRes

    // 피드 수정
    @PUT("/api/feeds/{id}")
    suspend fun editFeed(
        @Path("id") feedId: Long,
        @Body body: FeedCreateReq
    ): FeedRecommendRes

    @DELETE("/api/feeds/{id}")
    suspend fun deleteFeed(
        @Path("id") feedId: Long
    ): DeleteFeedRes // ← 아래 data class 참고
}
