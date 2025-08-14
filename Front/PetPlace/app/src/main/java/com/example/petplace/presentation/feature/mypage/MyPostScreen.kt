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
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.ImageRes
import com.example.petplace.presentation.feature.feed.ProfileImage
import com.example.petplace.presentation.feature.feed.categoryStyles
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale

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
                color = Color.White,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF9F0))  // Feed와 동일한 배경색
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(6.dp)  // Feed와 동일한 간격
                ) {
                    items(uiState.posts) { post ->
                        MyPostFeedItem(feed = post)  // 새로운 컴포넌트 사용
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPostFeedItem(feed: FeedRecommendRes) {
    val hashtagColor = Color(0xFFF79800)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        // 프로필 & 카테고리
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            ProfileImage(feed.userImg)
            Spacer(Modifier.width(8.dp))
            Column {
                val (bgCol, txtCol) = categoryStyles[feed.category]
                    ?: (Color.LightGray to Color.DarkGray)

                Text(
                    feed.category,
                    fontSize = 12.sp,
                    color = txtCol,
                    modifier = Modifier
                        .background(bgCol, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(feed.userNick, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // 본문
        Text(feed.content, modifier = Modifier.padding(horizontal = 16.dp))

        // 태그
        if (!feed.tags.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.padding(horizontal = 16.dp)) {
                feed.tags.forEach { tag ->
                    Text(
                        "#${tag.name}",
                        color = hashtagColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // 이미지 영역
        if (feed.images.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.pp_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { feed.images!!.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img: ImageRes = feed.images!![page]
                    Image(
                        painter = rememberAsyncImagePainter("http://i13d104.p.ssafy.io:8081" + img.src),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "${pagerState.currentPage + 1}/${feed.images!!.size}",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.45f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 좋아요 / 댓글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            IconButton(onClick = { /* 내 게시글에서는 좋아요 기능 비활성화 */ }) {
                Icon(
                    imageVector = if (feed.liked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (feed.liked == true) Color(0xFFF44336) else LocalContentColor.current,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(text = "${feed.likes}", fontSize = 15.sp)

            Spacer(Modifier.width(15.dp))

            IconButton(onClick = { /* 댓글 보기 기능 비활성화 */ }) {
                Icon(
                    painter = painterResource(R.drawable.outline_chat_bubble_24),
                    contentDescription = "댓글",
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(text = "${feed.commentCount}", fontSize = 15.sp)
        }
    }
}