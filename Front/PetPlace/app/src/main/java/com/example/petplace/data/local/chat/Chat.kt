package com.example.petplace.data.local.chat

data class Chat(
    val name: String,
    val lastMessage: String,
    val time: String
)

data class ChatMessage(
    val content: String,
    val isFromMe: Boolean
)
