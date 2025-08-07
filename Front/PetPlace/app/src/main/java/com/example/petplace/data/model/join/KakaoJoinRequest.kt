package com.example.petplace.data.model.join

import com.example.petplace.data.model.login.KakaoJoinRequest

data class KakaoJoinRequest(
    val provider: String = "KAKAO",
    val tempToken : String ,
    val userInfo: KakaoJoinRequest.UserInfo,
    val impUid: String,
    val nickname: String,
    val regionId: Long)

data class UserInfo(
    val socialId: String,
    val email: String,
    val nickname: String,
    val profileImage: String
)
