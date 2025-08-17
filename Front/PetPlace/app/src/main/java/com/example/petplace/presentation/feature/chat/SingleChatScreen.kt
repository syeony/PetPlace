package com.example.petplace.presentation.feature.chat

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatMessage
import com.example.petplace.presentation.common.theme.PrimaryColor
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.LayoutDirection

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
    val chatPartnerId by viewModel.chatPartnerId.collectAsState()
    val chatPartnerName by viewModel.chatPartnerName.collectAsState()
    val chatPartnerProfileImage by viewModel.chatPartnerProfileImage.collectAsState()
    val imageUploadStatus by viewModel.imageUploadStatus.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.sendImageMessage(uris)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 생명주기 관찰자 추가
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "🔄 화면 Resume - WebSocket 활성화")
                    viewModel.onScreenVisible()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "⏸️ 화면 Pause - WebSocket 대기")
                    viewModel.onScreenHidden()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    // 키보드 숨김 처리
    LaunchedEffect(showAttachmentOptions) {
        if (showAttachmentOptions) {
            keyboardController?.hide()
        }
    }


    Scaffold(
        containerColor = Color.White,
        topBar = {
            ChatTopAppBar(
                chatPartnerName = chatPartnerName ?: "로딩 중...",
                chatPartnerProfileImage = chatPartnerProfileImage,
                isConnected = connectionStatus,
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileClick = {
                    navController.navigate("userProfile/${chatPartnerId}")
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    PaddingValues(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                )
        ) {
//            // Top App Bar
//            ChatTopAppBar(
//                chatPartnerName = chatPartnerName ?: "로딩 중...",
//                chatPartnerProfileImage = chatPartnerProfileImage,
//                isConnected = connectionStatus,
//                onBackClick = {
//                    navController.popBackStack()
//                },
//                onProfileClick = {
//                    navController.navigate("userProfile/${chatPartnerId}")
//                }
//            )

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 연결 상태 표시 개선
                if (!connectionStatus) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF856404)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "서버에 연결 중입니다...",
                                    color = Color(0xFF856404),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                items(
                    items = messages,
                    key = { message -> message.id ?: "${message.content}_${message.timestamp}" }
                ) { msg ->
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
                                MessageBubble(
                                    message = msg,
                                    backgroundColor = bgColor,
                                    textColor = textColor,
                                    isFromMe = true
                                )
                            } else {
                                MessageBubble(
                                    message = msg,
                                    backgroundColor = bgColor,
                                    textColor = textColor,
                                    isFromMe = false
                                )
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

            // Bottom Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
            ) {
                // Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            if (showAttachmentOptions) {
                                viewModel.closeAttachmentOptions()
                            } else {
                                keyboardController?.hide()
                                focusRequester.freeFocus()
                                viewModel.toggleAttachmentOptions()
                            }
                        }
                    ) {
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
                        ),
                        maxLines = 3
                    )

                    Spacer(Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (connectionStatus && messageInput.isNotBlank()) {
                                viewModel.sendMessage()
                                // 키보드 숨기기 및 첨부 옵션 닫기
                                keyboardController?.hide()
                                if (showAttachmentOptions) {
                                    viewModel.closeAttachmentOptions()
                                }
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

                // Attachment Options
                AnimatedVisibility(
                    visible = showAttachmentOptions,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    AttachmentOptionsGrid(
                        onCloseClick = { viewModel.closeAttachmentOptions() },
                        onOptionSelected = { option ->
                            when (option) {
                                "album" -> {
                                    imagePickerLauncher.launch("image/*")
                                }

                                else -> {
                                    Log.d(TAG, "첨부 옵션 선택: $option")
                                }
                            }
                            viewModel.closeAttachmentOptions()
                        }
                    )
                    // 업로드 상태 표시
                    when (imageUploadStatus) {
                        is ChatViewModel.ImageUploadStatus.Uploading -> {
                            // 로딩 표시 (원하는 곳에 배치)
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }

                        is ChatViewModel.ImageUploadStatus.Error -> {
                            // 에러 메시지는 이미 addSystemMessage로 처리됨
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    backgroundColor: Color,
    textColor: Color,
    isFromMe: Boolean
) {
    Log.d("MessageBubble", "렌더링: type=${message.messageType}, urls=${message.imageUrls}")

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = if (isFromMe) 18.dp else 4.dp,
            bottomEnd = if (isFromMe) 4.dp else 18.dp
        ),
        shadowElevation = 1.dp,
        border = if (!isFromMe) BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f)) else null
    ) {
        when (message.messageType) {
            ChatViewModel.MessageType.TEXT -> {
                Text(
                    text = message.content,
                    color = textColor,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }

            ChatViewModel.MessageType.IMAGE -> {
                if (message.imageUrls.isNotEmpty()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // 이미지 개수에 따라 다른 레이아웃
                        when (message.imageUrls.size) {
                            1 -> {
                                // 단일 이미지
                                AsyncImage(
                                    model = "http://43.201.108.195:8081" + message.imageUrls[0],
                                    contentDescription = "전송된 이미지",
                                    modifier = Modifier
                                        .widthIn(max = 200.dp)
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    onError = { error ->
                                        Log.e(
                                            "AsyncImage",
                                            "이미지 로드 실패: ${message.imageUrls[0]}",
                                            error.result.throwable
                                        )
                                    },
                                    onSuccess = {
                                        Log.d("AsyncImage", "이미지 로드 성공: ${message.imageUrls[0]}")
                                    }
                                )
                            }

                            else -> {
                                // 여러 이미지 - 그리드
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    items(message.imageUrls) { imageUrl ->
                                        AsyncImage(
                                            model = "http://43.201.108.195:8081" + imageUrl,
                                            contentDescription = "전송된 이미지",
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop,
                                            onError = { error ->
                                                Log.e(
                                                    "AsyncImage",
                                                    "이미지 로드 실패: $imageUrl",
                                                    error.result.throwable
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 이미지 URL이 없는 경우 - 디버깅용
                    Text(
                        text = "이미지 URL이 없습니다",
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
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
            AttachmentOption("앨범", R.drawable.outline_photo_library_24, "album"),
            AttachmentOption("카메라", R.drawable.outline_photo_camera_24, "camera"),
            AttachmentOption("자주쓰는문구", R.drawable.ic_chat, "text_phrases"),
            AttachmentOption("장소", R.drawable.ic_location_on, "location"),
            AttachmentOption("약속", R.drawable.outline_schedule_24, "appointment"),
            AttachmentOption("페이", R.drawable.outline_paid_24, "pay"),
        )

        val columns = 3
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
    chatPartnerProfileImage: String? = null,
    isConnected: Boolean = true,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White
            ),
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
            title = { Text("") },
            modifier = Modifier.height(48.dp),
            // ✅ status bar 공간 없애기
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                //.padding(top = 12.dp),
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = chatPartnerProfileImage,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_mypage),
                error = painterResource(id = R.drawable.ic_mypage)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chatPartnerName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (isConnected) Color.Green else Color.Red,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isConnected) "연결됨" else "연결 중...",
                    fontSize = 10.sp,
                    color = if (isConnected) Color.Green else Color.Gray
                )
            }
        }
    }
}
