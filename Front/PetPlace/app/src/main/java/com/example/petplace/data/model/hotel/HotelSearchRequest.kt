package com.example.petplace.data.model.hotel


data class HotelSearchRequest(
    val petType: String,      // 반려동물 종류
    val startDate: String,    // 체크인 날짜
    val endDate: String,      // 체크아웃 날짜
    val minPrice: Int,        // 최소 가격
    val maxPrice: Int,        // 최대 가격
    val region: String,       // 지역명
    val latitude: Double,     // 위도
    val longitude: Double,    // 경도
    val radiusKm: Int         // 검색 반경 (km)
)