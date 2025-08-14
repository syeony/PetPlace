package com.example.petplace.data.model.hotel


data class HotelSearchRequest(
    val petType: String,      // 반려동물 종류
    val startDate: String,    // 체크인 날짜
    val endDate: String,      // 체크아웃 날짜
    val minPrice: Int? = 0,        // 최소 가격
    val maxPrice: Int? = 1000000,        // 최대 가격
    val region: String? = null,       // 지역명
    val latitude: Double? = null,     // 위도
    val longitude: Double? = null,    // 경도
    val radiusKm: Int? = null         // 검색 반경 (km)
)