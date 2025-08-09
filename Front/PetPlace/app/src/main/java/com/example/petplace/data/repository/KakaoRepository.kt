package com.example.petplace.data.repository

import com.example.petplace.BuildConfig
import com.example.petplace.data.remote.KakaoApiService
import com.example.petplace.data.remote.KeywordDocument
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KakaoRepository @Inject constructor(
    private val api: KakaoApiService
) {
    /** 위경도로 동(행정동/법정동) 이름 한 줄로 반환 */
    suspend fun getRegionByCoord(lat: Double, lng: Double): String {
        val resp = api.getRegionByCoord(lng, lat)
        return resp.documents.firstOrNull()?.address_name ?: "주소 없음"
    }
    /** 키워드 검색 → 장소 리스트 반환 */
    suspend fun searchPlaces(
        keyword: String,
        x: Double? = null,
        y: Double? = null,
        radius: Int? = 1000
    ): List<KeywordDocument> {
        // Header("Authorization")는 Hilt에서 Interceptor로 넣거나 직접 넘겨도 됨
        val resp = api.searchPlace(
            keyword = keyword,
            x = x,
            y = y,
            radius = radius
        )
        return resp.documents
    }
}
