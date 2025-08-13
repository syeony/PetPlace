package com.example.petplace.data.remote

import com.example.petplace.data.model.pet.PetRes
import com.example.petplace.data.model.pet.PetInfoResponse
import com.example.petplace.data.model.pet.PetRequest
import com.example.petplace.data.model.pet.PetResponse
import com.example.petplace.data.model.pet.PetUpdateRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PetApiService {

    @POST("/api/pets")
    suspend fun addPet(
        @Body request: PetRequest
    ): Response<PetResponse>

    @PUT("/api/pets/{id}")
    suspend fun updatePet(
        @Path("id") id: Int,
        @Body request: PetUpdateRequest
    ): Response<PetResponse>

    @GET("/api/pets/{id}")
    suspend fun getPetInfo(@Path("id") petId: Int): Response<PetInfoResponse>

    @GET("/api/pets/me")
    suspend fun getMyPets(): List<PetRes>

}