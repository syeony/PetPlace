package com.example.petplace.data.remote

import com.example.petplace.data.model.Pet.PetRes
import retrofit2.http.GET

interface PetApiService {
    @GET("/api/pets/me")
    suspend fun getMyPets(): List<PetRes>
}