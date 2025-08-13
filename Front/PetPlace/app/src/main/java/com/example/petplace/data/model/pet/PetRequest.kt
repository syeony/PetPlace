package com.example.petplace.data.model.pet

data class PetRequest(
    val name: String,
    val animal: String,      // "DOG", "CAT" 등
    val breed: String,       // "POMERANIAN" 등
    val sex: String,         // "MALE", "FEMALE"
    val birthday: String,    // "2025-08-12" 형식
    val imgSrc: String?,
    val tnr: Boolean
)