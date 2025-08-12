package com.example.petplace.data.model.mypage

data class ProfileIntroductionResponse(
    val id: Int,
    val user_id: Int,
    val user_name: String,
    val content: String
)