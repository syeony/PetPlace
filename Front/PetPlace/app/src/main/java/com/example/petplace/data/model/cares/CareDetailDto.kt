package com.example.petplace.data.model.cares

import com.example.petplace.data.model.feed.ImageRes
import com.example.petplace.data.model.mypage.MyPageInfoResponse


data class CareDetailDto(
    val id: Long,
    val title: String,
    val content: String,
    val userId: Long,
    val userNickname: String,
    val userImg: String,
    val petId: Long,
    val petName: String,
    val petBreed: String,
    val petImg: String,
    val animalType: String,
    val regionId: Long,
    val regionName: String,
    val category: String,              // 예: "WALK_WANT"
    val categoryDescription: String,
    val status: String,                // 예: "ACTIVE"
    val startDatetime: String,         // "2025-08-12T12:49:50.470Z"
    val endDatetime: String,
    val views: Int,
    val createdAt: String,
    val updatedAt: String,
    val images: List<MyPageInfoResponse.ImageInfo>
)

