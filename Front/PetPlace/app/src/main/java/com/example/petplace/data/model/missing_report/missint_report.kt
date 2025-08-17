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
data class ImageRes(
    val id: Long,
    val src: String,
    val sort: Int
)

data class SightingImage(
    val src: String,
    val sort: Int
)

data class SightingRequest(
    val regionId: Long,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val sightedAt: String, // ISO 8601 (예: "2025-08-15T21:55:13.400Z")
    val images: List<SightingImage>,
    val species: String,
    val breedEng: String,
    val xmin: Int,
    val ymin: Int,
    val xmax: Int,
    val ymax: Int,
    val wface: Double
)
data class SightingImageRes(
    val id: Long,
    val src: String,
    val sort: Int
)

data class SightingData(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImg: String,
    val regionId: Long,
    val regionName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val breed: String,
    val sightedAt: String,  // ISO 8601
    val createdAt: String,  // ISO 8601
    val images: List<SightingImageRes>
)

data class SightingResponse(
    val success: Boolean,
    val message: String,
    val data: SightingData
)
data class MissingReportDetailDto(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImg: String?,
    val regionId: Long,
    val regionName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val breed: String?,
    val sightedAt: String,  // ISO8601
    val createdAt: String,  // ISO8601
    val images: List<SightingImageDto>
)

data class SightingImageDto(
    val id: Long,
    val src : String,
    val sort : Int
)


