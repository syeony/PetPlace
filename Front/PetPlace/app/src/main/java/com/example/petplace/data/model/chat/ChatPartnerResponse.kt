package com.example.petplace.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatPartnerResponse(
    @SerializedName("id")
    val userId: Long,
    val nickname: String,
    @SerializedName("profileImg")
    val profileImageUrl: String? = null,
    @SerializedName("regionName")
    val region: String? = null
)
