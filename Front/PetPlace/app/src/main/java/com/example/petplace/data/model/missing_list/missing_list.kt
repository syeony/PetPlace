package com.example.petplace.data.model.missing_list

import com.example.petplace.data.model.missing_report.ImageRes

// 페이지 공통 구조
data class PageResponse<T>(
    val totalElements: Int,
    val totalPages: Int,
    val pageable: Pageable,
    val size: Int,
    val content: List<T>,
    val number: Int,
    val sort: Sort,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

data class Pageable(
    val paged: Boolean,
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Int,
    val sort: Sort,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean,
    val empty: Boolean,
    val unsorted: Boolean
)

// 리스트 아이템
data class MissingReportDto(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImg: String,
    val petId: Long,
    val petName: String,
    val petBreed: String,
    val petImg: String,
    val regionId: Long,
    val regionName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val content: String,
    val status: String,
    val missingAt: String, // ISO 문자열 ("2025-08-13T07:19:36.296Z")
    val createdAt: String, // ISO 문자열
    val images: List<ImageRes>
)