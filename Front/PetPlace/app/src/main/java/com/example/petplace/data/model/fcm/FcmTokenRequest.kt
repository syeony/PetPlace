package com.example.petplace.data.model.fcm

data class FcmTokenRequest(
    val token: String,
    val appVersion: String
)

data class FcmTokenResponse(
    val id: Long,
    val token: String,
    val appVersion: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
)