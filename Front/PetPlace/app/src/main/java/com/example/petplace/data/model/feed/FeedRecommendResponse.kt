package com.example.petplace.data.model.feed

import com.google.gson.annotations.SerializedName

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
    val likes: Int,
    val views: Int,
    val score: Double,
    val tags: List<TagRes>,
    @SerializedName("images") val images: List<ImageRes>,
    val comments: List<CommentRes>,
    val commentCount: Int
)
