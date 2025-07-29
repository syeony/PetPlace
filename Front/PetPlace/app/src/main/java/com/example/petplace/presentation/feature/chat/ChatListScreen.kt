package com.example.petplace.presentation.feature.chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatRoom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClick: (String) -> Unit) {
    val chatList = listOf(
        ChatRoom(1, "홍길동", "인의동", "안녕하세요! 잘 지내시죠?", "오전 10:15", 3, R.drawable.ic_mypage),
        ChatRoom(2, "이순신", "진평동", "오늘 회의는 몇 시죠?", "오전 9:50", 1, R.drawable.ic_mypage),
        ChatRoom(3, "김유신", "강남동", "보낸 자료 확인 부탁드려요.", "어제", 0, R.drawable.ic_mypage),
        ChatRoom(4, "장보고", "역삼동", "감사합니다!", "어제", 0, R.drawable.ic_mypage),
        ChatRoom(5, "세종대왕", "논현동", "회의록 정리했습니다.", "7월 27일", 0, R.drawable.ic_mypage)
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = "채팅", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            items(chatList) { chat ->
                ChatItem(chat, onClick = { onChatClick(chat.name) })

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    ChatListScreen(
        onChatClick = { name -> println("Clicked: $name") }
    )
}