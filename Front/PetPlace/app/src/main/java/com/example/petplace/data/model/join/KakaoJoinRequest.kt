package com.example.petplace.data.model.join

import com.example.petplace.data.model.login.KakaoJoinRequest

data class KakaoJoinRequest(
    val provider: String = "KAKAO",
    val tempToken : String ,
    val impUid: String,
    val nickname: String,
    val regionId: Long
)
