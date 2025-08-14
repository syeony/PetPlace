package com.example.petplace.data.model.join

data class NeighborhoodResponse(
    val success: Boolean,
    val message: String,
    val data: RegionData?
)

data class RegionData(
    val regionId: Long,
    val regionName: String
)