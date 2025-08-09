package com.example.petplace.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 기존 Region 코드 매핑
data class RegionResponse(val documents: List<RegionDocument>)
data class RegionDocument(val address_name: String)


// 키워드 검색 응답 모델
data class KeywordSearchResponse(
    val documents: List<KeywordDocument>
)
data class KeywordDocument(
    val place_name: String,
    val x: String,  // 경도
    val y: String   // 위도
)

interface KakaoApiService {
    /** 경도(x), 위도(y) → 행정동/법정동 코드 결과 */
    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getRegionByCoord(
        @Query("x") longitude: Double,
        @Query("y") latitude : Double
    ): RegionResponse

    /** 키워드 검색: 장소 정보 조회 */
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(
        @Query("query") keyword: String,
        @Query("x") x: Double? = null,
        @Query("y") y: Double? = null,
        @Query("radius") radius: Int? = null
    ): KeywordSearchResponse
}
