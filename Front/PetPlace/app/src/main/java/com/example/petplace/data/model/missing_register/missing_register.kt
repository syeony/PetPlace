package com.example.petplace.data.model.missing_register

import com.example.petplace.data.model.missing_report.ImageRes

data class CreateRegisterImageReq(
    val src: String,
    val sort: Int
)

data class CreateRegisterReq(
    val petId: Long,
    val regionId: Long,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val missingAt: String,
    val images: List<CreateRegisterImageReq>
)

data class RegisterRes(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImg: String?,
    val petId: Long,
    val petName: String,
    val petBreed: String,
    val petImg: String,
    val regionId: Long,
    val regionName: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val status: String,
    val missingAt: String,
    val createdAt: String,
    val images: List<ImageRes>
)