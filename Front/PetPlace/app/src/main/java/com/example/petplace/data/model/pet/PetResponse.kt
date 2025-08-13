package com.example.petplace.data.model.pet

data class PetResponse(
    val id: Int,
    val name: String,
    val age: Int,
    val imgSrc: String?
)