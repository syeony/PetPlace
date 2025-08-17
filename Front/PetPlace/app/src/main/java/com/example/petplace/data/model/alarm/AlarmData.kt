package com.example.petplace.data.model.alarm

data class AlarmData(
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val refType: String? = null,
    val refId: String? = null,
    val chatId: String? = null,
    val userId: String? = null,
    val profileImage: String? = null,
    val animalImage: String? = null
)