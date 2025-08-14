package com.example.petplace.data.model.hotel

data class HotelReservationRequest(
    val petId: Int,
    val hotelId: Int,
    val selectedDates: List<String>,
    val specialRequests: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalDays: Int,
    val consecutiveDates: Boolean
)
