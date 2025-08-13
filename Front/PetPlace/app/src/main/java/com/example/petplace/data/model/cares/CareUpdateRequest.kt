package com.example.petplace.data.model.cares

data class CareUpdateRequest(
    val title: String,
    val content: String,
    val petId: Long,
    val regionId: Long,
    val category: String,          // or enum: "WALK_WANT" 등
    val startDate: String,         // "YYYY-MM-DD"
    val endDate: String,           // "YYYY-MM-DD"
    val startTime: String,         // "HH:mm"
    val endTime: String,           // "HH:mm"
    val imageUrls: List<String> = emptyList()
)