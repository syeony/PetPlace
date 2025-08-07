package com.example.petplace.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatMessageDTO(
    @SerializedName("chatRoomId")
    val chatRoomId: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("message")
    val message: String,

    @SerializedName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("chatId")
    val chatId: Long? = null
)