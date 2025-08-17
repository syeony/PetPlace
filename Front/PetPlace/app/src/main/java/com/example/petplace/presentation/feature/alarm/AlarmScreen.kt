package com.example.petplace.presentation.feature.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.utils.AlarmManager
import java.text.SimpleDateFormat
import java.util.*

data class MessageItem(
    val id: String,
//    val profileImage: Int,
//    val animalImage: Int,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val refType: String? = null,
    val refId: String? = null,
    val chatId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(navController: NavController) {
    val viewModel: AlarmViewModel = hiltViewModel()
    val alarms by viewModel.alarms.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    // 화면이 시작될 때 알림 목록 로드
    LaunchedEffect(Unit) {
        viewModel.loadAlarms()
    }

    // 알림을 MessageItem으로 변환
    val messages = alarms.map { alarm ->
        MessageItem(
            id = alarm.id,
//            profileImage = getProfileImageByType(alarm.refType),
//            animalImage = getAnimalImageByType(alarm.refType),
            message = alarm.message,
            timeAgo = formatTimestamp(alarm.timestamp),
            isRead = alarm.isRead,
            refType = alarm.refType,
            refId = alarm.refId,
            chatId = alarm.chatId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "알림",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(25.dp)
                                    .background(Color.Red, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Gray
                        )
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.clearAllAlarms()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "알림 모두 삭제",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (messages.isEmpty()) {
            // 알림이 없을 때 표시
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "알림이 없습니다",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    MessageItemRow(
                        message = message,
                        onClick = {
                            // 알림 클릭 시 읽음 처리
                            viewModel.markAsRead(message.id)

                            // 해당 페이지로 네비게이션
                            handleAlarmClick(navController, message)
                        }
                    )
                    Divider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItemRow(
    message: MessageItem,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        // 프로필 이미지
        Box {

            // 메시지 내용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (message.isRead) Color.Gray else Color.Black,
                    fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timeAgo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // 읽지 않은 알림 표시
            if (!message.isRead) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }

    }
}

// 유틸리티 함수들
private fun getProfileImageByType(refType: String?): Int {
    return when (refType?.uppercase()) {
        "CHAT" -> R.drawable.ic_mypage
        "FEED" -> R.drawable.ic_mypage
        "SIGHTING" -> R.drawable.ic_mypage
        else -> R.drawable.ic_mypage
    }
}

private fun getAnimalImageByType(refType: String?): Int {
    return when (refType?.uppercase()) {
        "CHAT" -> R.drawable.sample_hamster
        "FEED" -> R.drawable.sample_hamster
        "SIGHTING" -> R.drawable.outline_sound_detection_dog_barking_24
        else -> R.drawable.sample_hamster
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "방금 전"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}시간 전"
        else -> "${diff / (24 * 60 * 60 * 1000)}일 전"
    }
}

private fun handleAlarmClick(navController: NavController, message: MessageItem) {
    when (message.refType?.uppercase()) {
        "CHAT" -> {
            val chatId = message.refId ?: message.chatId
            chatId?.toLongOrNull()?.let { id ->
                navController.navigate("chatDetail/$id")
            } ?: navController.navigate("chat")
        }
        "FEED" -> {
            message.refId?.toLongOrNull()?.let { id ->
                navController.navigate("feedDetail/$id")
            } ?: navController.navigate("feed")
        }
        "SIGHTING" -> {
            message.refId?.toLongOrNull()?.let { id ->
                navController.navigate("missingReportDetail/$id")
            } ?: navController.navigate("missing_list")
        }
        else -> {
            // 기본적으로 피드로 이동
            navController.navigate("feed")
        }
    }
}
