package com.example.petplace.data.model.login

data class KakaoLoginRequest(
    val provider: String = "KAKAO",
    val accessToken: String
)

