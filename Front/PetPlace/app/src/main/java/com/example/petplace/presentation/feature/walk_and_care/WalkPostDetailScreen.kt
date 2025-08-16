package com.example.petplace.presentation.feature.walk_and_care

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.feature.feed.categoryStyles
import kotlin.math.max

private const val BASE = "http://i13d104.p.ssafy.io:8081"

@Composable
fun WalkPostDetailScreen(
    navController: NavController,
    postId: Long,
    viewModel: WalkAndCareDetailViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val currentUserId = app.getUserInfo()?.userId ?: 0
    LaunchedEffect(postId) { viewModel.load(postId) }

    LaunchedEffect(ui.createdChatRoomId) {
        ui.createdChatRoomId?.let { chatRoomId ->
            navController.navigate("chatDetail/$chatRoomId")
            viewModel.consumeCreatedChatRoomId()
        }
    }

    val orange = Color(0xFFF79800)
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            }
        },
        bottomBar = {
            val isMyPost = ui.data?.userId == PetPlaceApp.getAppContext().let { app ->
                (app as? PetPlaceApp)?.getUserInfo()?.userId ?: 0
            }

            if (!isMyPost) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            ui.data?.userId?.let { userId ->
                                viewModel.startChatWithUser(userId)
                            }
                        },
                        enabled = !ui.isChatRoomCreating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange)
                    ) {
                        if (ui.isChatRoomCreating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("채팅하기", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        when {
            ui.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            ui.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { Text(ui.error!!, color = Color.Red) }
            }

            ui.data != null -> {
                val data = ui.data!!

                // ✅ categoryDescription 완전 무시, enum만 매핑
                val categoryKo = displayLabelFromCategoryEnum(data.category?.name)
                val avatar = fullUrlOrNull(data.userImg) ?: R.drawable.pp_logo
                val images: List<Any> =
                    data.images
                        .sortedBy { it.sort }
                        .map { img -> fullUrlOrNull(img.src) ?: R.drawable.pp_logo }
                        .ifEmpty { listOf(R.drawable.pp_logo) }

                val dateText = formatDateRange(data.startDatetime, data.endDatetime)
                val timeText = formatTimeRange(data.startDatetime, data.endDatetime)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 88.dp)
                ) {
                    // 헤더
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            AsyncImage(
                                model = avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val style = categoryStyles[categoryKo]
                                    ?: (Color(0xFFFFF4E5) to Color(0xFF8A5800))
                                Text(
                                    text = categoryKo,
                                    color = style.second,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .background(style.first, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(text = data.userNickname ?: "", fontSize = 18.sp)
                            }
                        }
                    }

                    // 제목
                    item {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = data.title,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // 본문 (좌우 패딩)
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = data.content,
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    // 이미지 슬라이드: 풀블리드 + 정사각형 + 내부 오버레이
                    item {
                        val pagerState = rememberPagerState(initialPage = 0, pageCount = { max(images.size, 1) })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.Black)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = images.getOrNull(page)
                                        ?: "https://via.placeholder.com/800x800.png?text=No+Image",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (images.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(images.size) { idx ->
                                        val selected = pagerState.currentPage == idx
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(if (selected) 8.dp else 6.dp)
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(if (selected) Color(0xFFF79800) else Color(0xFFE5E7EB))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 날짜/시간
                    item {
                        Spacer(Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = Color(0xFFF79800))
                                Spacer(Modifier.width(6.dp))
                                Text("날짜", fontSize = 13.sp, color = Color(0xFF6B7280))
                                Spacer(Modifier.width(10.dp))
                                Text(dateText, fontSize = 13.5.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFFF79800))
                                Spacer(Modifier.width(6.dp))
                                Text("시간", fontSize = 13.sp, color = Color(0xFF6B7280))
                                Spacer(Modifier.width(10.dp))
                                Text(timeText.ifBlank { "-" }, fontSize = 13.5.sp)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(6.dp)) }
                }
            }
        }
    }
}

/* ====== 헬퍼 ====== */

private fun fullUrlOrNull(s: String?): String? {
    val p = s?.trim().orEmpty()
    if (p.isBlank() || p.equals("null", true)) return null
    return if (p.startsWith("http")) p else "$BASE${if (p.startsWith("/")) "" else "/"}$p"
}

private fun fullUrl(s: String?): String {
    val p = s?.trim().orEmpty()
    return if (p.startsWith("http")) p else "$BASE${if (p.startsWith("/")) "" else "/"}$p"
}

private fun formatDateRange(start: String?, end: String?): String {
    val s = start?.takeIf { it.length >= 10 }?.substring(5, 10)?.replace("-", ".")
    val e = end?.takeIf { it.length >= 10 }?.substring(5, 10)?.replace("-", ".")
    return when {
        !s.isNullOrBlank() && !e.isNullOrBlank() -> "$s ~ $e"
        !s.isNullOrBlank() -> s
        !e.isNullOrBlank() -> e
        else -> "-"
    }
}

private fun extractTime(dt: String?): String? {
    return try {
        val tPart = dt?.split('T', ' ')?.getOrNull(1) ?: return null
        tPart.substring(0, 5) // HH:mm
    } catch (_: Exception) { null }
}

private fun formatTimeRange(start: String?, end: String?): String {
    val st = extractTime(start)
    val et = extractTime(end)
    return when {
        !st.isNullOrBlank() && !et.isNullOrBlank() -> "$st ~ $et"
        !st.isNullOrBlank() -> st
        !et.isNullOrBlank() -> et
        else -> "-"
    }
}

// ✅ enum → 화면 라벨 (categoryDescription 무시)
private fun displayLabelFromCategoryEnum(enumName: String?): String =
    when (enumName?.uppercase()) {
        "WALK_WANT" -> "산책구인"
        "WALK_REQ"  -> "산책의뢰"
        "CARE_WANT" -> "돌봄구인"
        "CARE_REQ"  -> "돌봄의뢰"
        else        -> "산책구인"
    }
