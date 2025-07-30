package com.example.petplace.presentation.feature.chat

import android.R.id
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatMessage
import com.example.petplace.presentation.common.theme.PrimaryColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    chatPartnerName: String,
    messages: List<ChatMessage> = sampleMessages,
    navController: NavController
) {
    var messageInput by remember { mutableStateOf("") }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ChatTopAppBar(
                chatPartnerName,
                onBackClick = {
                    navController.popBackStack()
                })
        },
        bottomBar = {
            AnimatedVisibility( // Animate visibility of attachment options
                visible = showAttachmentOptions,
                enter = expandVertically(expandFrom = Alignment.Bottom),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                AttachmentOptionsGrid(
                    onCloseClick = { showAttachmentOptions = false }, // Close button callback
                    onOptionSelected = { /* Handle option selection */ } // Callback for individual options
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    showAttachmentOptions = !showAttachmentOptions
                }) {
                    Icon(
                        imageVector = if (showAttachmentOptions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showAttachmentOptions) "Close" else "Add",
                        tint = Color.Black
                    )
                }
                TextField(
                    value = messageInput,
                    onValueChange = { newValue -> messageInput = newValue },
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
                .padding(horizontal = 8.dp)
                .imePadding(),
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
                        Row(verticalAlignment = Alignment.Bottom) {
                            if (msg.isFromMe) {
                                // 내가 보낸 메시지일 경우: 읽음/시간 -> 메시지 버블
                                Column {
                                    Text(
                                        text = "읽음", // 실제 시간으로 변경 가능
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .align(Alignment.End)
                                    )
                                    Text(
                                        text = "오후 2:30", // 실제 시간으로 변경 가능
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp)) // 가로 간격으로 변경
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
                            } else {
                                // 상대방이 보낸 메시지일 경우: 메시지 버블 -> 읽음/시간
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
                                Spacer(modifier = Modifier.width(2.dp)) // 가로 간격으로 변경
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
    }
}

// Composable for attachment options grid
@Composable
fun AttachmentOptionsGrid(
    onCloseClick: () -> Unit,
    onOptionSelected: (String) -> Unit // Callback for selected option (e.g., "album", "camera")
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close (X) button
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "메시지 보내기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f) // Push text to center
            )
            // Empty Spacer to balance the layout if needed, or remove for simple right alignment
            Spacer(modifier = Modifier.width(24.dp)) // Adjust as needed to align "메시지 보내기"

            // Smile icon (You might need to add a custom drawable for this)
            Icon(
                painter = painterResource(id = R.drawable.ic_mypage), // Replace with your smile icon drawable
                contentDescription = "이모티콘",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* 전송 로직 */ }) { // Send button
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "전송",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid for attachment options
        // Using FlowRow from Accompanist if available, otherwise a nested Row/Column or LazyVerticalGrid
        // For simplicity, let's use nested Rows for a fixed grid for now.
        // For dynamic/more flexible grids, consider using Accompanist Flow Layouts or a custom layout.
        val options = listOf(
            AttachmentOption("앨범", R.drawable.ic_mypage, "album"), // Replace with your actual drawables
            AttachmentOption("카메라", R.drawable.ic_map, "camera"),
            AttachmentOption("자주쓰는문구", R.drawable.ic_chat, "text_phrases"),
            AttachmentOption("장소", R.drawable.ic_board, "location"),
            AttachmentOption("약속", R.drawable.ic_home, "appointment"),
            AttachmentOption("당근페이", R.drawable.ic_mypage, "carrot_pay"),
            AttachmentOption("편의점택배", R.drawable.ic_chat, "parcel_delivery"),
            AttachmentOption("선물하기", R.drawable.ic_home, "gift")
        )

        // Assuming 4 columns, adjust as needed
        val columns = 4
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.chunked(columns).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround // Distribute items evenly
                ) {
                    rowOptions.forEach { option ->
                        AttachmentOptionItem(option = option, onClick = { onOptionSelected(option.key) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Space between rows
            }
        }
    }
}

// Data class for an attachment option
data class AttachmentOption(val name: String, val iconResId: Int, val key: String)

// Composable for a single attachment option item
@Composable
fun AttachmentOptionItem(option: AttachmentOption, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp) // Smaller padding around clickable area
            .width(IntrinsicSize.Min) // Allows content to define width
    ) {
        Box(
            modifier = Modifier
                .size(52.dp) // Icon background size
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)), // Light gray background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = option.iconResId),
                contentDescription = option.name,
                tint = Color.Unspecified, // Keep original icon tint if it's a colored drawable
                modifier = Modifier.size(24.dp) // Icon size
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = option.name, fontSize = 11.sp, color = Color.Gray)
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
        ),
        navController = TODO()
    )
}