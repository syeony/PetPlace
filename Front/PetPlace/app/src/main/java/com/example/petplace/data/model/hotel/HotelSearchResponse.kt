package com.example.petplace.data.model.hotel

data class HotelSearchResponse(
    val success: Boolean,
    val message: String,
    val data: List<HotelDetail>
)