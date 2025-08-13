package com.example.petplace.data.model.mypage

data class ProfileUpdateRequest(
    val nickname: String?,
    val curPassword: String?,
    val newPassword: String?,
    val imgSrc: String?,
    val regionId: Long?
)