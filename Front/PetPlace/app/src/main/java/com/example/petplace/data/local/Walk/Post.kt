package com.example.petplace.data.local.Walk

data class Post(
    val category: String,
    val title: String,
    val body: String,
    val date: String,
    val time: String,
    val imageUrl: String, // ✅ Drawable 대신 URL
    val reporterName: String,
    val reporterAvatarUrl:String
)

data class WalkWriteForm(
    val category: String,
    val title: String,
    val details: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val image: String?
)