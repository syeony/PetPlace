package com.example.petplace.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatRoomResponse(
    @SerializedName("chatRoomId")
    val chatRoomId: Long,
    @SerializedName("userId1")
    val userId1: Long,
    @SerializedName("userId2")
    val userId2: Long,
    @SerializedName("lastMessage")
    val lastMessage: String?,
    @SerializedName("lastMessageAt")
    val lastMessageAt: String?
)

// 사용자 정보 (임시로 간단하게)
data class User(
    val userId: Long,
    val name: String,
    val profileImageUrl: Int? = null,
    val region: String? = null
)