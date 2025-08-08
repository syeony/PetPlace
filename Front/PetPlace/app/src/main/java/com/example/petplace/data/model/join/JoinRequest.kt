package com.example.petplace.data.model.join

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class JoinRequest(
    @SerializedName("userName")
    val userName: String,

    @SerializedName("password")
    val password: String,
    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("regionId")
    val regionId: Long,
    //41만  100000

    @SerializedName("impUid")
    val impUid: String
)
