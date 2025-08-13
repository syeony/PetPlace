package com.example.petplace.data.model.cares

import com.example.petplace.data.model.feed.ImageRes
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query


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

enum class CareCategory {
    @SerializedName("WALK_WANT") WALK_WANT,
    @SerializedName("WALK_REQ") WALK_OFFER,
    @SerializedName("CARE_WANT") CARE_WANT,
    @SerializedName("CARE_REQ") CARE_REQ
}

enum class CareStatus {
    @SerializedName("ACTIVE") ACTIVE,
    @SerializedName("CLOSED") CLOSED,
    @SerializedName("CANCELED") CANCELED
}

// 목록 카드용 요약
// (스웨거에 페이지 래퍼가 있으면 Page<T>로 감싸서 바꾸세요)

data class CareSummary(
    val id: Long,
    val title: String,
    val content: String,
    val userId: Long,
    val userNickname: String?,
    val userImg: String?,
    val petId: Long,
    val petName: String?,
    val petBreed: String?,
    val petImg: String?,
    val animalType: String?,
    val regionId: Long,
    val regionName: String?,
    val category: CareCategory,
    val categoryDescription: String?,
    val status: CareStatus,
    val startDatetime: String?, // ISO-8601 string, e.g. 2025-08-12T14:30:00
    val endDatetime: String?,
)

// 상세 응답 (필요 시 서버 스키마에 맞춰 확장)

data class CareDetail(
    val id: Long,
    val title: String,
    val content: String,
    val userId: Long,
    val userNickname: String?,
    val userImg: String?,
    val petId: Long,
    val petName: String?,
    val petBreed: String?,
    val petImg: String?,
    val animalType: String?,
    val regionId: Long,
    val regionName: String?,
    val category: CareCategory,
    val categoryDescription: String?,
    val status: CareStatus,
    val startDatetime: String?,
    val endDatetime: String?,
    val imageUrls: List<String> = emptyList()
)

// 생성 요청 — 스웨거 예시를 그대로 반영

data class CareCreateRequest(
    val title: String,
    val content: String,
    val petId: Long,
    val regionId: Long,
    val category: CareCategory,
    val startDate: String? = null,  // ← nullable
    val endDate: String? = null,    // ← nullable
    val startTime: String? = null,  // ← nullable
    val endTime: String? = null,    // ← nullable
    val imageUrls: List<String> = emptyList()
)



data class CareUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val petId: Long? = null,
    val regionId: Long? = null,
    val category: CareCategory? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val imageUrls: List<String>? = null,
    val status: CareStatus? = null
)

// 생성/수정 이후 서버가 반환하는 식별자/데이터 래퍼 (프로젝트에 이미 ApiResponse<>가 있으면 그대로 사용하세요)

data class IdResponse(val id: Long)

data class PageResponse<T>(
    val totalElements: Long,
    val totalPages: Int,
    val pageable: Pageable? = null,
    val size: Int,
    val content: List<T>,
    val number: Int,
    val sort: Sort? = null,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)
data class Pageable(
    val paged: Boolean,
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Long,
    val sort: Sort,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean,
    val empty: Boolean,
    val unsorted: Boolean
)
interface CaresApiService {
    @GET("/api/cares")
    suspend fun getCares(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("regionId") regionId: Long? = null,
        @Query("category") category: CareCategory? = null,
        @Query("sort") sort: String? = "createdAt,desc"
    ): retrofit2.Response<ApiResponse<PageResponse<CareItem>>> // ★ 여기!
}
data class CareItem(
    val id: Long,
    val title: String?,
    val userId: Long?,
    val userNickname: String?,
    val userImg: String?,
    val petId: Long?,
    val petName: String?,
    val petImg: String?,
    val animalType: String?,
    val regionName: String?,
    val category: String?,              // WALK_WANT, CARE_WANT 등
    val categoryDescription: String?,   // ← 이거 추가
    val status: String?,
    val startDatetime: String?,
    val endDatetime: String?,
    val views: Long?,
    val createdAt: String?
)
