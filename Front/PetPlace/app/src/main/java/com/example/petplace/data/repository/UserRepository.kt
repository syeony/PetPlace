package com.example.petplace.data.repository

import DongAuthResponse

interface UserRepository {
    suspend fun authenticateDong(lat: Double, lon: Double): DongAuthResponse
}