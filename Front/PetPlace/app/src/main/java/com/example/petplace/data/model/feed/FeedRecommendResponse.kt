package com.example.petplace.data.model.feed

import com.google.gson.annotations.SerializedName

//삭제할 피드아이디
data class DeleteFeedRes(
    val id: Long
)

//댓글
data class CommentReq(
    val feedId: Long,
    val parentCommentId: Long?, // null 가능
    val content: String
)
data class DeleteCommentRes(
    val id: Long
)

// 좋아요
data class LikesRes(
    val feedId: Long,
    val feedLikes: Int
)
data class LikeFeedReq(
    val feedId: Long
)

// 태그
data class TagRes(
    val id: Long,
    val name: String
)

// 이미지
data class ImageRes(
    val src: String,
    val sort: Int
)

// 댓글 (대댓글 재귀)
data class CommentRes(
    val id: Long,
    @SerializedName("parentCommentId") val parentId: Long?,
    val feedId: Long,
    val content: String,
    val userId: Long,
    val userNick: String,
    val userImg: String?,
    val createdAt: String,
    val updatedAt: String?,
    val deletedAt: String?,
    val replies: List<CommentRes> = emptyList() //서버가 주지 않으면 빈 배열
)

// 피드
data class FeedRecommendRes(
    val id: Long,
    val content: String,
    val userId: Long,
    val userNick: String,
    val userImg: String?,
    val regionId: Long,
    val category: String,
    val createdAt: String,
    val updatedAt: String?,
    val deletedAt: String?,
    val liked: Boolean?,
    val likes: Int,
    val views: Int,
    val score: Double,
    val tags: List<TagRes>?,
    val images: List<ImageRes>,
    val comments: List<CommentRes> ,
    val commentCount: Int
)
