package com.example.petplace.presentation.feature.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.presentation.common.navigation.BottomBar

@Composable
private fun ChatListScreen(onChatClick: (String) -> Unit) {
    Scaffold(
        bottomBar = { BottomBar() }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "채팅",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // 상단 고정 배너
            Card(
                backgroundColor = Color(0xFFF9C56F),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_warning), contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("실종견을 찾고 있어요!", fontWeight = FontWeight.Bold)
                        Text("포메라니안 · 인의동 근처에서 실종", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 채팅 목록
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sampleChats) { chat ->
                    ChatItem(chat) { onChatClick(chat.userName) }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Image(
            painter = painterResource(id = chat.profileImage),
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.userName, fontWeight = FontWeight.SemiBold)
            Text(chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(chat.time, fontSize = 12.sp, color = Color.Gray)
    }
}
