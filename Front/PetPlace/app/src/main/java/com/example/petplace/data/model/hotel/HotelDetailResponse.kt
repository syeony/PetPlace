package com.example.petplace.data.model.hotel

data class HotelDetailResponse(
    val success: Boolean,
    val message: String,
    val data: HotelDetail
)

data class HotelDetail(
    val id: Int,
    val name: String,
    val description: String,
    val address: String,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerNight: Int,
    val totalPrice: Int,
    val maxCapacity: Int,
    val supportedPetTypes: List<String>,
    val imageUrl: String
)
