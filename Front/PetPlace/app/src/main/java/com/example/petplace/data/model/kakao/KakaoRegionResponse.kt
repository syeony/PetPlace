package com.example.petplace.data.model.kakao

data class KakaoRegionResponse(
    val documents: List<Document>
) {
    data class Document(
        val region_3depth_name: String
    )
}
