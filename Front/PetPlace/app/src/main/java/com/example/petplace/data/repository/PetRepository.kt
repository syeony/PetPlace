package com.example.petplace.data.repository

import com.example.petplace.data.model.Pet.PetRes
import com.example.petplace.data.remote.PetApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val api: PetApiService
) {
    suspend fun getMyPets(): List<PetRes> = api.getMyPets()
}