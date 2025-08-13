package com.example.petplace.data.model.pet

data class PetRes(
    val id: Long,
    val name: String,
    val animal: String,   // "DOG" 등 (서버 enum을 문자열로 받음)
    val breed: String,    // "POMERANIAN" 등
    val sex: String,      // "MALE" | "FEMALE"
    val birthday: String?,// "2025-08-12" or null
    val imgSrc: String?,  // 이미지 URL
    val tnr: Boolean
)