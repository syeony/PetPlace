package com.example.petplace.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class RegionResponse(val documents: List<RegionDocument>)
data class RegionDocument(val address_name: String)

interface KakaoApiService {
    /** 경도(x), 위도(y) → 행정동/법정동 코드 결과 */
    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getRegionByCoord(
        @Query("x") longitude: Double,
        @Query("y") latitude : Double
    ): RegionResponse
}
