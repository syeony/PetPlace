package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.ImageRes
import com.example.petplace.presentation.feature.feed.BoardViewModel
import com.example.petplace.presentation.feature.feed.CommentBottomSheet
import com.example.petplace.presentation.feature.feed.ProfileImage
import com.example.petplace.presentation.feature.feed.categoryStyles

// 메인 화면
@Composable
fun MyPostScreen(
    navController: NavController,
    viewModel: MyPostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✨ Edit 화면에서 돌아올 때 갱신
    val navBackStackEntry = navController.currentBackStackEntry
    val feedEdited = navBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>("feedEdited")
        ?.observeAsState()

    LaunchedEffect(feedEdited?.value) {
        if (feedEdited?.value == true) {
            viewModel.refreshPosts() // 또는 loadMyPosts()
            navBackStackEntry?.savedStateHandle?.remove<Boolean>("feedEdited")
        }
    }

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
                        text = "내 피드",
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        MyPostFeedItem(
                            feed = post,
                            viewModel = viewModel,
                            onEditFeed = { feedId, regionId ->
                                navController.navigate("board/edit/$feedId/$regionId")
                            },
                            onDeleteFeed = { feedId ->
                                viewModel.deleteMyFeed(feedId)
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun MyPostFeedItem(
    feed: FeedRecommendRes,
    viewModel: MyPostViewModel = hiltViewModel(),
    viewModel2: BoardViewModel = hiltViewModel(),
    onEditFeed: (Long, Long) -> Unit,
    onDeleteFeed: (Long) -> Unit
) {
    val hashtagColor = Color(0xFFF79800)

    var showComments by remember { mutableStateOf(false) }
    val likedSet by viewModel.likedFeeds.collectAsState()
    val liked = likedSet.contains(feed.id) || feed.liked == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        // 프로필 & 카테고리
        Box(modifier = Modifier.fillMaxWidth()) {
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

            // 👇 여기 추가: 더보기 메뉴
            var showMenu by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp, end = 8.dp)
            ) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("수정") },
                        onClick = {
                            onEditFeed(feed.id, feed.regionId) // regionId는 FeedRecommendRes에 있음
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteFeed(feed.id)
                        }
                    )
                }
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
                    .aspectRatio(1f) // ✅ 정방형 유지
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { feed.images!!.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // ✅ 정방형 유지
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
            IconButton(onClick = { viewModel.toggleLike(feed) }) {
                Icon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (liked) Color(0xFFF44336) else LocalContentColor.current,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(text = "${feed.likes}", fontSize = 15.sp)

            Spacer(Modifier.width(15.dp))

            IconButton(onClick = { showComments = true }) {
                Icon(
                    painter = painterResource(R.drawable.outline_chat_bubble_24),
                    contentDescription = "댓글",
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(text = "${feed.commentCount}", fontSize = 15.sp)
        }
    }

    if (showComments) { //테스트
        CommentBottomSheet(
            feedId = feed.id,
            onDismiss = { showComments = false },
            viewModel = viewModel2
        )
    }


}