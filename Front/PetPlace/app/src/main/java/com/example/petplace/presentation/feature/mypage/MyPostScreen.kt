package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

// 메인 화면
@Composable
fun MyPostScreen(
    navController: NavController,
    viewModel: MyPostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 표시 로직
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFAF4E6),
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "내 게시글",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF000000),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.posts) { post ->  // 변경된 부분
                        MyPostCard(
                            post = post,
                            onDeleteClick = { viewModel.deletePost(post.id) }
                        )
                    }
                }
            }

            // Pull to refresh 추가 (선택사항)
            if (uiState.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

// 게시글 카드 UI
@Composable
fun MyPostCard(post: Post,onDeleteClick: (() -> Unit)? = null) {
    val style = categoryStyles[post.category] ?: Pair(Color.LightGray, Color.DarkGray)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "#${post.category}",
                    color = style.second,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(style.first, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.body,
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = post.meta,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = post.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat), // 댓글 아이콘
                        contentDescription = "댓글",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentCount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// 샘플 더미 데이터
val samplePosts = listOf(
    Post(
        category = "잡담",
        title = "오늘 날씨 너무 좋네요",
        body = "하늘이 맑고 바람도 시원해서 산책하기 딱 좋은 날씨입니다.",
        meta = "2025.08.10",
        imageRes = R.drawable.pp_logo,
        commentCount = 5
    ),
    Post(
        category = "질문",
        title = "Jetpack Compose 리스트 간격 조절 질문",
        body = "LazyColumn에서 아이템 간 간격을 조절하는 방법을 알고 싶습니다.",
        meta = "2025.08.09",
        imageRes = R.drawable.pp_logo,
        commentCount = 8
    ),
    Post(
        category = "정보",
        title = "안드로이드 스튜디오 최신 단축키 모음",
        body = "효율적으로 개발할 수 있는 단축키 리스트를 정리했습니다.",
        meta = "2025.08.08",
        imageRes = R.drawable.pp_logo,
        commentCount = 12
    )
)
