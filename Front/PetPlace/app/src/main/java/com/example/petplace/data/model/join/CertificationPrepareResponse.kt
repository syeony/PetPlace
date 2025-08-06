package com.example.petplace.data.model.join

import com.google.gson.annotations.SerializedName

data class CertificationPrepareResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: CertificationData
)

data class CertificationData(
    @SerializedName("merchant_uid")
    val merchantUid: String,

    @SerializedName("certification_url")
    val certificationUrl: String
)
