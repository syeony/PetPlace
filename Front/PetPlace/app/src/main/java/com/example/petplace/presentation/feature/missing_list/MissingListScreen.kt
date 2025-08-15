// MissingListScreen.kt
package com.example.petplace.presentation.feature.missing_list

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.PetPlaceApp
import com.example.petplace.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MissingListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MissingListViewModel = hiltViewModel()
) {
    val bgColor = Color(0xFFFEF9F0)
    val orange  = Color(0xFFF79800)
    val ui by viewModel.ui.collectAsState()

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val currentUserId = app.getUserInfo()?.userId ?: 0

    LaunchedEffect(ui.createdChatRoomId) {
        ui.createdChatRoomId?.let { chatRoomId ->
            navController.navigate("chatDetail/$chatRoomId")
            viewModel.consumeCreatedChatRoomId()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 상단 뒤로가기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        }

        when {
            ui.isLoading && ui.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            ui.error != null && ui.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(text = ui.error ?: "오류가 발생했습니다.") }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.items, key = { it.id }) { item ->
                        MissingCard(
                            item = item,
                            orange = orange,
                            currentUserId = currentUserId, // 추가
                            isChatCreating = ui.isChatRoomCreating, // 추가
                            onChatClick = {
                                viewModel.startChatWithUser(item.userId)
                            }
                        )
                    }

                    if (ui.hasMore && !ui.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { viewModel.loadNext() },
                                    colors = ButtonDefaults.buttonColors(containerColor = orange),
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("더 보기", color = Color.White) }
                            }
                        }
                    }

                    if (ui.isLoading && ui.items.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun MissingCard(
    item: MissingReportUi,
    orange: Color,
    currentUserId: Long,
    isChatCreating: Boolean,
    onChatClick: () -> Unit
) {
    Card(
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 프로필 + 이름
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarPainter = item.reporterAvatarUrl?.let {
                    rememberAsyncImagePainter(it)
                } ?: androidx.compose.ui.res.painterResource(R.drawable.pp_logo)

                Image(
                    painter = avatarPainter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(item.reporterName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            // 내용
            Text(
                text = item.content,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // 큰 사진
            Spacer(Modifier.height(10.dp))
            val photoPainter = item.photoUrl?.let { rememberAsyncImagePainter(it) }
            if (photoPainter != null) {
                Image(
                    painter = photoPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)  // ✅ 1:1 정방형
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 사진 없을 때 로고로 대체
                Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.pp_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)  // ✅ 1:1 정방형
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // 목격 정보
            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                InfoRow(
                    icon = Icons.Default.DateRange,
                    iconTint = orange,
                    label = "목격 일시",
                    value = item.seenAt
                )
                Spacer(Modifier.height(6.dp))
                InfoRow(
                    icon = Icons.Filled.Place,
                    iconTint = orange,
                    label = "목격 장소",
                    value = item.location
                )
            }

            // 채팅하기 버튼 (본인 게시글이 아닐 때만 표시)
            if (item.userId != currentUserId) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onChatClick,
                    enabled = !isChatCreating,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange)
                ) {
                    if (isChatCreating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("생성 중...", color = Color.White, fontSize = 15.sp)
                    } else {
                        Text("채팅하기", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFF6B7280), fontSize = 13.sp)   // 회색 라벨
        Spacer(Modifier.width(8.dp))
        Text(value, fontSize = 13.5.sp)
    }
}
