package com.example.petplace.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatReadDTO(
    @SerializedName("chatRoomId")
    val chatRoomId: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("lastReadCid")
    val lastReadCid: Long
)