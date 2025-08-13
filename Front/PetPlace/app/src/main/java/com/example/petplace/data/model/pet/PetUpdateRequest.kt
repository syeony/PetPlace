package com.example.petplace.data.model.pet

data class PetUpdateRequest(
    val name: String,
    val animal: String,
    val breed: String,
    val sex: String,
    val birthday: String,
    val imgSrc: String?,
    val tnr: Boolean
)
