package com.example.petplace.presentation.feature.chat

import android.content.ContentValues.TAG
import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    chatPartnerName: String,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messageInput by viewModel.messageInput.collectAsState()
    val showAttachmentOptions by viewModel.showAttachmentOptions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // 화면이 표시될 때 읽음 처리
    LaunchedEffect(Unit) {
        Log.d(TAG, "SingleChatScreen: 화면 진입")
        viewModel.markMessagesAsRead()
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                chatPartnerName = chatPartnerName,
                isConnected = connectionStatus,
                onBackClick = {
                    navController.popBackStack()
                })
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = showAttachmentOptions,
                    enter = expandVertically(expandFrom = Alignment.Bottom),
                    exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    AttachmentOptionsGrid(
                        onCloseClick = { viewModel.closeAttachmentOptions() },
                        onOptionSelected = { /* Handle option selection */ }
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
                        if (showAttachmentOptions) {
                            viewModel.toggleAttachmentOptions()
                        } else {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                            viewModel.toggleAttachmentOptions()
                        }
                    }) {
                        Icon(
                            imageVector = if (showAttachmentOptions) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showAttachmentOptions) "Close" else "Add",
                            tint = Color.Black
                        )
                    }

                    TextField(
                        value = messageInput,
                        onValueChange = { viewModel.onMessageInputChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                if (connectionStatus) "메시지를 입력하세요..." else "연결 중...",
                                color = Color(0xFFADAEBC)
                            )
                        },
                        enabled = connectionStatus, // 연결상태에 따라 입력 가능/불가능
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF3F4F6),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (connectionStatus) {
                                viewModel.sendMessage()
                            }
                        },
                        enabled = connectionStatus && messageInput.isNotBlank(),
                        modifier = Modifier
                            .background(
                                color = if (connectionStatus && messageInput.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.Gray,
                                shape = RoundedCornerShape(14.dp)
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
            // 연결 상태 표시
            if (!connectionStatus) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                    ) {
                        Text(
                            text = "서버에 연결 중입니다...",
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFF856404)
                        )
                    }
                }
            }

            items(messages) { msg ->
                val alignment = if (msg.isFromMe) Arrangement.End else Arrangement.Start
                val bgColor = if (msg.isFromMe) PrimaryColor else Color.White
                val textColor = if (msg.isFromMe) Color.White else Color.Black

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = alignment
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (msg.isFromMe) {
                            // 내가 보낸 메시지일 경우: 읽음/시간 -> 메시지 버블
                            Column {
                                Text(
                                    text = "읽음",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .align(Alignment.End)
                                )
                                Text(
                                    text = if (msg.timestamp.isNotEmpty()) msg.timestamp else "방금",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
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
                            Spacer(modifier = Modifier.width(2.dp))
                            Column {
                                Text(
                                    text = "읽음",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = if (msg.timestamp.isNotEmpty()) msg.timestamp else "방금",
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

// 나머지 기존 함수들은 그대로 유지...

@Composable
fun AttachmentOptionsGrid(
    onCloseClick: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    // 기존 AttachmentOptionsGrid 코드 그대로 사용
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
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(24.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_mypage),
                contentDescription = "이모티콘",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* 전송 로직 */ }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "전송",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val options = listOf(
            AttachmentOption("앨범", R.drawable.ic_mypage, "album"),
            AttachmentOption("카메라", R.drawable.ic_map, "camera"),
            AttachmentOption("자주쓰는문구", R.drawable.ic_chat, "text_phrases"),
            AttachmentOption("장소", R.drawable.ic_board, "location"),
            AttachmentOption("약속", R.drawable.ic_home, "appointment"),
            AttachmentOption("당근페이", R.drawable.ic_mypage, "carrot_pay"),
            AttachmentOption("편의점택배", R.drawable.ic_chat, "parcel_delivery"),
            AttachmentOption("선물하기", R.drawable.ic_home, "gift")
        )

        val columns = 4
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.chunked(columns).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rowOptions.forEach { option ->
                        AttachmentOptionItem(option = option, onClick = { onOptionSelected(option.key) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

data class AttachmentOption(val name: String, val iconResId: Int, val key: String)

@Composable
fun AttachmentOptionItem(option: AttachmentOption, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .width(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = option.iconResId),
                contentDescription = option.name,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
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
    isConnected: Boolean = true,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Box {
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
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = "로그아웃"
                    )
                }
            },
            title = { Text("") }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mypage),
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
            // 연결 상태 표시
            if (!isConnected) {
                Text(
                    text = "연결 중...",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}