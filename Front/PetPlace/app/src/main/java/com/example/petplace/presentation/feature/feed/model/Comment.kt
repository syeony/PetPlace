package com.example.petplace.presentation.feature.feed.model

data class Comment(
    val postId: String,
    val author: String,
    val profileImage: String,
    val town: String,
    val text: String,
    val isMine: Boolean,
    val replies: List<Reply> = emptyList()
)

data class Reply(
    val author: String,
    val profileImage: String,
    val town: String,
    val text: String,
    val isMine: Boolean
)
