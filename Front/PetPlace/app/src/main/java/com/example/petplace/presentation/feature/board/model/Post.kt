package com.example.petplace.presentation.feature.board.model

data class Post(
    val profileImage: String,
    val category: String,
    val author: String,
    val content: String,
    val hashtags: List<String>,
    val imageUrl: String,
    val location: String,
    val likes: Int,
    val comments: Int
)
