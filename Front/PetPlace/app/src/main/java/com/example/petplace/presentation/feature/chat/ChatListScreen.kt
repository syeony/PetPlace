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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.petplace.R
import com.example.petplace.data.local.chat.ChatRoom
import com.example.petplace.presentation.common.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (Long) -> Unit, // chatRoomId를 전달하도록 변경
    viewModel: ChatListViewModel = hiltViewModel()
) {

    val chatRooms by viewModel.chatRooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val isCreatingChatRoom by viewModel.isCreatingChatRoom.collectAsState()
    val chatRoomCreated by viewModel.chatRoomCreated.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateChatDialog by remember { mutableStateOf(false) }

    // 에러 메시지 표시
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // 채팅방 생성 성공 메시지 표시
    LaunchedEffect(chatRoomCreated) {
        chatRoomCreated?.let { chatRoom ->
            snackbarHostState.showSnackbar(
                message = "채팅방이 생성되었습니다! (ID: ${chatRoom.chatRoomId})",
                duration = SnackbarDuration.Short
            )
            viewModel.clearChatRoomCreated()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // 최신 viewModel을 참조하는 안전한 방식
    val currentViewModel by rememberUpdatedState(viewModel)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 💡 화면이 다시 포커스를 받을 때마다 호출됨
                currentViewModel.refreshChatRooms()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 채팅방 생성 다이얼로그
    if (showCreateChatDialog) {
        CreateChatRoomDialog(
            isLoading = isCreatingChatRoom,
            onDismiss = { showCreateChatDialog = false },
            onCreateChatRoom = { userId1, userId2 ->
                viewModel.createChatRoom(userId1, userId2)
                showCreateChatDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "채팅",
                        style = AppTypography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { showCreateChatDialog = true },
//                containerColor = MaterialTheme.colorScheme.primary
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "채팅방 생성",
//                    tint = Color.White
//                )
//            }
//        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val image = painterResource(id = R.drawable.dog_example)

            // 상단 배너
            MissingPetCard(
                imagePainter = image,
                onClick = { /* TODO: 이동 처리 */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 로딩 상태 표시
//            if (isLoading) {
//                Box(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.padding(end = 8.dp)
//                        )
//                        Text("채팅방 목록을 불러오는 중...")
//                    }
//                }
//            }

            // 채팅방 목록 또는 빈 상태 표시
            if (!isLoading) {
                if (chatRooms.isEmpty()) {
                    // 빈 상태 표시
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "아직 채팅방이 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "새로운 대화를 시작해보세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // 채팅방 목록 표시
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(chatRooms) { chat ->
                            ChatItem(
                                chat = chat,
                                onClick = { onChatClick(chat.id) } // chatRoomId 전달
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChatRoomDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateChatRoom: (String, String) -> Unit
) {
    var userId1 by remember { mutableStateOf("") }
    var userId2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text("채팅방 생성")
        },
        text = {
            Column {
                Text(
                    text = "채팅방을 생성할 두 사용자의 ID를 입력해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = userId1,
                    onValueChange = { userId1 = it },
                    label = { Text("첫 번째 사용자 ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userId2,
                    onValueChange = { userId2 = it },
                    label = { Text("두 번째 사용자 ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userId1.isNotBlank() && userId2.isNotBlank()) {
                        onCreateChatRoom(userId1.trim(), userId2.trim())
                    }
                },
                enabled = !isLoading && userId1.isNotBlank() && userId2.isNotBlank()
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("생성 중...")
                    }
                } else {
                    Text("생성")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("취소")
            }
        }
    )
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
            .padding(horizontal = 16.dp)
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
    // Preview에서는 더미 데이터 사용
    val dummyChatRooms = listOf(
        ChatRoom(1, "홍길동", "인의동", "안녕하세요! 잘 지내시죠?", "오전 10:15", 3, null),
        ChatRoom(2, "이순신", "진평동", "오늘 회의는 몇 시죠?", "오전 9:50", 1, null)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val image = painterResource(id = R.drawable.dog_example)
        MissingPetCard(imagePainter = image)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(dummyChatRooms) { chat ->
                ChatItem(chat, onClick = { })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}