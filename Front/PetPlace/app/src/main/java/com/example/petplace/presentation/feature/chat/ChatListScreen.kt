package com.example.petplace.presentation.feature.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // ✅ 전체에 적용
                .background(Color.White)
        ) {
            val image = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24)

            // ✅ 상단 배너
            MissingPetCard(
                imagePainter = image,
                onClick = { /* TODO: 이동 처리 */ }
            )

            Spacer(modifier = Modifier.height(12.dp)) // ✅ 배너 아래 여백 추가

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // ✅ 내부 간격만 부여
            ) {
                items(chatList) { chat ->
                    ChatItem(chat, onClick = { onChatClick(chat.name) })
                    Spacer(modifier = Modifier.height(8.dp)) // ✅ 채팅 항목 간 여백
                }
            }
        }
    }
}


@Composable
fun MissingPetCard(
    modifier: Modifier = Modifier,
    imagePainter: Painter,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row {
            // 이미지 영역
            Image(
                painter = imagePainter,
                contentDescription = "잃어버린 강아지",
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )

            // 텍스트 + 배경 그라디언트
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFCB8C2E), Color(0xFFF4D58D))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "실종견을 찾고 있어요",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "포메라니안 · 인의동 근처에서 실종",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "이동",
                        tint = Color.White
                    )
                }
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