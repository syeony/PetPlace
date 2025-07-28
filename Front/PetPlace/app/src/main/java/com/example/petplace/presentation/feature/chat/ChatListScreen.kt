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
import com.example.petplace.data.local.chat.Chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClick: (String) -> Unit) {
    val chatList = listOf(
        Chat("홍길동", "안녕하세요! 잘 지내시죠?", "오전 10:15"),
        Chat("이순신", "오늘 회의는 몇 시죠?", "오전 9:50"),
        Chat("김유신", "보낸 자료 확인 부탁드려요.", "어제"),
        Chat("장보고", "감사합니다!", "어제"),
        Chat("세종대왕", "회의록 정리했습니다.", "7월 27일")
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
                Divider()
            }
        }
    }
}
