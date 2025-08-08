package com.example.petplace.data.model.login

import com.google.gson.annotations.SerializedName

class KakaoJoinRequest (
    val provider: String,    // ex: "KAKAO"
    val tempToken: String,
    val userInfo: UserInfo,
    val nickname: String,
    //41만  100000
    val regionId: Long,
    val impUid: String

) {
    data class UserInfo(
        var socialId: Long,     // 카카오에서 받은 user.id
        val email: String,        // 카카오 계정 이메일
        val nickname: String,     // 카카오 프로필 닉네임
        val profileImage: String  // 카카오 프로필 이미지 URL
    )

}

