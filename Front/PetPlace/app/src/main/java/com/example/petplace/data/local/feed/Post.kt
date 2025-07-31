package com.example.petplace.data.local.feed

data class Post(
    val id: String,
    val profileImage: String,
    val category: String,
    val author: String,
    val content: String,
    val hashtags: List<String>,
    val imageUrls: List<String>,
    val location: String,
    val likes: Int,
    val comments: Int
)
