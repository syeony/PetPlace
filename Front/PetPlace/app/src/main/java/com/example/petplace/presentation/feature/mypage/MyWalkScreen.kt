package com.example.petplace.presentation.feature.mypage

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.presentation.feature.feed.categoryStyles
import com.example.petplace.presentation.feature.walk_and_care.navigateToWalkDetail

/**
 * 내 산책 화면 (산책구인 + 산책의뢰)
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyWalkScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyWalkAndCareViewModel = hiltViewModel()
) {
    val walkPosts by viewModel.walkPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val orange = Color(0xFFF79800)

    LaunchedEffect(Unit) {
        viewModel.fetchMyWalkPosts()
    }

    // 작성 성공 감지 → 새로고침
    val handle = navController.currentBackStackEntry?.savedStateHandle
    val postCreated by remember(handle) {
        handle?.getStateFlow("walk_post_created", false)
    }?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(postCreated) {
        if (postCreated) {
            viewModel.fetchMyWalkPosts()
            handle?.remove<Boolean>("walk_post_created")
            handle?.remove<Long>("walk_post_id")
        }
    }

    Scaffold(
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
                Text(
                    text = "내 산책",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = orange)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("내 산책 게시글을 불러오는 중...", color = Color.Gray)
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "오류가 발생했습니다",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            error!!,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                walkPosts.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "아직 작성한 산책 글이 없습니다",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "산책구인이나 산책의뢰 글을 작성해보세요!",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(walkPosts) { post ->
                            WalkPostCard(
                                post = post,
                                onClick = { navController.navigateToWalkDetail(post) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkPostCard(
    post: Post,
    onClick: () -> Unit
) {
    val orange = Color(0xFFF79800)

    Surface(
        color = Color(0xFFFFFCF9),  // 산책용 따뜻한 배경색
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                // 카테고리 태그 (산책 전용 스타일)
                val style = categoryStyles[post.category] ?: run {
                    when (post.category) {
                        "산책구인" -> Pair(Color(0xFFFFE4B5), Color(0xFFD97706))
                        "산책의뢰" -> Pair(Color(0xFFDDEAFF), Color(0xFF1D4ED8))
                        else -> Pair(Color.LightGray, Color.DarkGray)
                    }
                }

                Text(
                    post.category,
                    color = style.second,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(color = style.first, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 제목
                Text(
                    post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 내용
                Text(
                    text = post.body,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 날짜 및 시간 정보
                Column {
                    InfoRow(
                        icon = Icons.Default.DateRange,
                        iconTint = orange,
                        label = "날짜",
                        value = post.date
                    )
                    Spacer(Modifier.height(4.dp))
                    InfoRow(
                        icon = Icons.Default.Info,
                        iconTint = orange,
                        label = "시간",
                        value = post.time
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 썸네일 이미지
            Image(
                painter = rememberAsyncImagePainter(
                    fullUrl(post.imageUrl)
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.Top)
            )
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
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(value, color = Color.Gray, fontSize = 12.sp)
    }
}

// 이미지 URL 처리 함수
private fun fullUrl(path: String?): Any {
    val p = path?.trim().orEmpty()
    if (p.isBlank() || p.equals("null", true)) return R.drawable.pp_logo
    return if (p.startsWith("http")) p else "http://i13d104.p.ssafy.io:8081" + (if (p.startsWith("/")) "" else "/") + p
}