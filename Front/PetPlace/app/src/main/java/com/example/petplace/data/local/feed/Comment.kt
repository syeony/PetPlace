package com.example.petplace.data.local.feed

data class Comment(
    val postId: String,
    val author: String,
    val profileImage: String,
    val town: String,
    val text: String,
    val isMine: Boolean,
    val replies: List<Reply> = emptyList(),
    val expanded : Boolean = false

)

data class Reply(
    val author: String,
    val profileImage: String,
    val town: String,
    val text: String,
    val isMine: Boolean
)
