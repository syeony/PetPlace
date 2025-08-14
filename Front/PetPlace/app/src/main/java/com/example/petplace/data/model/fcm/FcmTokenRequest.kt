package com.example.petplace.data.model.fcm

data class FcmTokenRequest(
    val userId: Long,
    val token: String,
    val deviceId: String? = null     // 선택: 다중기기 관리용
)