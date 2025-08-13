package com.example.petplace.data.model.missing_report

// ---------- Request ----------
data class CreateSightingImageReq(
    val src: String,
    val sort: Int
)

data class CreateSightingReq(
    val regionId: Long,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    /** ISO-8601 UTC (예: 2025-08-12T07:14:30.553Z) */
    val sightedAt: String,
    val images: List<CreateSightingImageReq>
)

// ---------- Response Wrapper ----------
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

// ---------- Response Data ----------
data class SightingImageRes(
    val id: Long,
    val src: String,
    val sort: Int
)

data class SightingRes(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImg: String?,
    val regionId: Long,
    val regionName: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val breed: String?,
    val sightedAt: String,
    val createdAt: String,
    val images: List<SightingImageRes>
)
