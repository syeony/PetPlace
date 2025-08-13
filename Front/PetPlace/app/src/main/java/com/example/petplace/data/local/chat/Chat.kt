package com.example.petplace.data.local.chat

import com.example.petplace.presentation.feature.chat.ChatViewModel

data class ChatRoom(
    val id: Long,              // 채팅방 ID (백엔드 연동 시 필수)
    val name: String,                // 사용자 이름
    val region: String,              // 동네 이름
    val lastMessage: String,         // 마지막 메시지 내용
    val time: String,                // 마지막 메시지 시간 (예: "3시간 전")
    val unreadCount: Int,            // 안읽은 메시지 수
    val profileImageUrl: String? = null // 프로필 이미지 URL (nullable)
)


data class ChatMessage(
    val id: Long?,
    val content: String,
    val messageType: ChatViewModel.MessageType = ChatViewModel.MessageType.TEXT,
    val imageUrls: List<String> = emptyList(),
    val isFromMe: Boolean,
    val timestamp: String,
    val isRead: Boolean
)