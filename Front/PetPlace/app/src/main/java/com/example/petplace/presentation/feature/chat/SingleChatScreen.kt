package com.example.petplace.presentation.feature.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.data.local.chat.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    chatPartnerName: String,
    messages: List<ChatMessage> = listOf(
        ChatMessage("안녕하세요!", false),
        ChatMessage("네, 반갑습니다.", true),
        ChatMessage("오늘 회의는 몇 시예요?", false),
        ChatMessage("오후 3시에 시작해요.", true),
        ChatMessage("감사합니다!", false)
    )
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = chatPartnerName) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isFromMe) Arrangement.End else Arrangement.Start
                val bgColor = if (msg.isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (msg.isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = alignment
                ) {
                    Surface(
                        color = bgColor,
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = msg.content,
                            color = textColor,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
