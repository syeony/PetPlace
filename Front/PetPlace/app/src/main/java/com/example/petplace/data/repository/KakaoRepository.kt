package com.example.petplace.data.repository

import com.example.petplace.data.remote.KakaoApiService
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
}
