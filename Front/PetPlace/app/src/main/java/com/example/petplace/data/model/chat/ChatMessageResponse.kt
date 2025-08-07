package com.example.petplace.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatMessageResponse(
    @SerializedName("chatId")
    val chatId: Long,
    @SerializedName("chatRoomId")
    val chatRoomId: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("imageUrls")
    val imageUrls: List<String>?,
    @SerializedName("createdAt")
    val createdAt: String
)
