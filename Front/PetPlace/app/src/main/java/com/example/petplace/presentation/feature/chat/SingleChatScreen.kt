package com.example.petplace.presentation.feature.chat

import android.R.id
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatMessage
import com.example.petplace.presentation.common.theme.PrimaryColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    chatPartnerName: String,
    messages: List<ChatMessage> = sampleMessages
) {
    Scaffold(
        topBar = {
            ChatTopAppBar(chatPartnerName)
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { /* 전송 로직 */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(28.dp)), // 둥근 모서리 적용
                    placeholder = { Text("메시지를 입력하세요...", color = Color(0xFFADAEBC)) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF3F4F6),
                        focusedIndicatorColor = Color.Transparent, // 포커스 시 밑줄 제거
                        unfocusedIndicatorColor = Color.Transparent, // 비포커스 시 밑줄 제거
                        disabledIndicatorColor = Color.Transparent // 비활성화 시 밑줄 제거
                    )
                )
                Spacer(Modifier.width(10.dp))
                IconButton(
                    onClick = { /* 전송 로직 */ },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(14.dp) // 둥근 모양
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isFromMe) Arrangement.End else Arrangement.Start
                val bgColor = if (msg.isFromMe) PrimaryColor else Color.White
                val textColor = if (msg.isFromMe) Color.White else Color.Black

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = alignment
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Surface(
                            color = bgColor,
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = msg.content,
                                color = textColor,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Column {
                            Text(
                                text = "읽음", // 실제 시간으로 변경 가능
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text(
                                text = "오후 2:30", // 실제 시간으로 변경 가능
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    chatPartnerName: String,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Box {
        // 실제 TopAppBar 구성
        SmallTopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            actions = {
                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(id = com.example.petplace.R.drawable.baseline_logout_24),
                        contentDescription = "로그아웃"
                    )
                }
            },
            title = {
                // 이 안의 Column은 좌측 정렬됨
                Text("") // title 비워두고 아래 Box에서 커스텀 중앙 타이틀 넣기
            }
        )

        // 완전한 가운데 정렬을 위한 오버레이
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = com.example.petplace.R.drawable.ic_mypage),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chatPartnerName,
                fontSize = 14.sp
            )
        }
    }
}


val sampleMessages = listOf(
    ChatMessage("아직 판매중이신가요?", false),
    ChatMessage("네, 아직 판매중입니다!", true),
    ChatMessage("직거래 가능한가요? 위치가 어디신가요?", false),
    ChatMessage("강남역 근처에서 직거래 가능해요!", true),
    ChatMessage("좋네요!", false),
    ChatMessage("내일 오후에 만날 수 있을까요?", false)
)


@Preview(showBackground = true)
@Composable
fun SingleChatScreenPreview() {
    SingleChatScreen(
        "김민수",
        listOf(
            ChatMessage("안녕하세요!", false),
            ChatMessage("네, 반갑습니다.", true),
            ChatMessage("오늘 회의는 몇 시예요?", false),
            ChatMessage("오후 3시에 시작해요.", true),
            ChatMessage("감사합니다!", false)
        )
    )
}