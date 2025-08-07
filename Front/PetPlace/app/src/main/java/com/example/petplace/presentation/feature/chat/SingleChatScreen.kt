package com.example.petplace.presentation.feature.chat

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.PrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SingleChatScreen(
    chatRoomId: Long,
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messageInput by viewModel.messageInput.collectAsState()
    val showAttachmentOptions by viewModel.showAttachmentOptions.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val chatPartnerName by viewModel.chatPartnerName.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 키보드 높이를 감지
    val density = LocalDensity.current
    val imeBottomHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottomHeight > 0

    // 메시지 목록이 변경되거나 키보드가 나타날 때마다 마지막으로 스크롤
    LaunchedEffect(messages.size, isKeyboardVisible) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(index = messages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            ChatTopAppBar(
                chatPartnerName = chatPartnerName ?: "로딩 중...",
                isConnected = connectionStatus,
                onBackClick = {
                    navController.popBackStack()
                }
            )

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f) // 남은 공간을 모두 차지
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(
                    bottom = 8.dp
                ),
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
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    ReadStatusIndicator(
                                        isRead = msg.isRead,
                                        timestamp = msg.timestamp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 18.dp,
                                        bottomEnd = 4.dp
                                    ),
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = msg.content,
                                        color = textColor,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 4.dp,
                                        bottomEnd = 18.dp
                                    ),
                                    shadowElevation = 1.dp,
                                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = msg.content,
                                        color = textColor,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))

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

            // Bottom Input Area - 수정된 부분: 순서 변경
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
            ) {
                // Input Row - 먼저 배치
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        enabled = connectionStatus,
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

                // Attachment Options - 나중에 배치 (입력창 아래에 표시됨)
                AnimatedVisibility(
                    visible = showAttachmentOptions,
                    enter = expandVertically(expandFrom = Alignment.Top), // 위에서 아래로 확장
                    exit = shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    AttachmentOptionsGrid(
                        onCloseClick = { viewModel.closeAttachmentOptions() },
                        onOptionSelected = { /* Handle option selection */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ReadStatusIndicator(
    isRead: Boolean,
    timestamp: String
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        // 읽음 상태 표시
        Text(
            text = if (isRead) "읽음" else "1",
            fontSize = 10.sp,
            color = if (isRead) Color.Gray else MaterialTheme.colorScheme.primary,
            fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold
        )

        // 시간 표시
        Text(
            text = if (timestamp.isNotEmpty()) timestamp else "방금",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun AttachmentOptionsGrid(
    onCloseClick: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {

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
                        AttachmentOptionItem(
                            option = option,
                            onClick = { onOptionSelected(option.key) })
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