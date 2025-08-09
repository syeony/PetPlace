package com.example.petplace.data.model.login

import com.google.gson.annotations.SerializedName

class KakaoJoinRequest (
    val provider: String = "KAKAO",    // ex: "KAKAO"
    val tempToken: String,
    val nickname: String,
    //41만  100000
    val regionId: Long,
    val impUid: String

) {

}

