package com.example.petplace.data.model.kakao

data class KakaoAddressResponse(
    val documents: List<Document>,
    val meta: Meta
) {
    data class Document(
        val address_name: String,
        val region_1depth_name: String,
        val region_2depth_name: String,
        val region_3depth_name: String,
        val x: String,
        val y: String
    )

    data class Meta(
        val is_end: Boolean,
        val pageable_count: Int,
        val total_count: Int
    )
}
