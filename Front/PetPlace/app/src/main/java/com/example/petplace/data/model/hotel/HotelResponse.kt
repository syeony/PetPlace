package com.example.petplace.data.model.hotel

import com.google.gson.annotations.SerializedName

data class HotelResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("pricePerNight")
    val pricePerNight: Int,

    @SerializedName("maxCapacity")
    val maxCapacity: Int,

    @SerializedName("supportedPetTypes")
    val supportedPetTypes: List<String>,

    @SerializedName("imageUrl")
    val imageUrl: String
)
