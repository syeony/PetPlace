package com.example.petplace.presentation.feature.hotel

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)