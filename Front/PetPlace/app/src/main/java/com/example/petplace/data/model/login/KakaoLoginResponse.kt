package com.example.petplace.data.model.login

import com.example.petplace.data.remote.LoginApiService

data class KakaoLoginResponse(
    val status: String,            // e.g. "EXISTING_USER"
    val message: String,           // 서버 응답 메시지
    val tokenDto: TokenDto,        // 로그인 성공 시 반환되는 토큰 정보
    val tempToken: String,         // 추가 임시 토큰 (필요 시)
    val linkableUserId: Int        // 연동 가능한 사용자 ID
) {
    data class TokenDto(
        val accessToken: String,
        val refreshToken: String,
        val message: String,
        val user: LoginApiService.User
    )

}
