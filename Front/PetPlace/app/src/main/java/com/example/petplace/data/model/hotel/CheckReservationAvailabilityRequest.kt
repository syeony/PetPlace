package com.example.petplace.data.model.hotel

data class CheckReservationAvailabilityRequest(
    val hotelId: Int,
    val selectedDates: List<String> // yyyy-MM-dd
)