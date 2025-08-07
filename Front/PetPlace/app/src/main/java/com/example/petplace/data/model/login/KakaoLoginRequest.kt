package com.example.petplace.data.model.login

data class KakaoLoginRequest(
    val provider: String,    // ex: "KAKAO"
    val userInfo: UserInfo
) {
    data class UserInfo(
        val socialId: String,     // 카카오에서 받은 user.id
        val email: String,        // 카카오 계정 이메일
        val nickname: String,     // 카카오 프로필 닉네임
        val profileImage: String  // 카카오 프로필 이미지 URL
    )
}

