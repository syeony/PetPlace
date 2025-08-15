package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.petplace.R
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.TagRes

@Composable
fun FeedDetailScreen(
    navController: NavController,
    feedId: Long,
    viewModel: FeedDetailViewModel = hiltViewModel()
) {
    val feed by viewModel.feed.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val likedFeeds by viewModel.likedFeeds.collectAsState()

    var showCommentsForFeedId by remember { mutableStateOf<Long?>(null) }

    // 피드 데이터 로드
    LaunchedEffect(feedId) {
        viewModel.loadFeedDetail(feedId)
    }

    // 에러 처리
    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            // 에러 처리 로직 (토스트, 스낵바 등)
        }
    }

    val hashtagColor = Color(0xFFF79800)

    fun moveToEditFeed(feedId: Long, regionId: Long) {
        navController.navigate("board/edit/$feedId/$regionId")
    }

    fun deleteFeed(feedId: Long) {
        viewModel.deleteFeed(feedId) {
            navController.popBackStack() // 삭제 후 이전 화면으로
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFEF9F0))
    ) {
        Column {
            // 상단 앱바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "피드 상세",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
            }

            // 피드 내용
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                feed != null -> {
                    FeedDetailContent(
                        feed = feed!!,
                        hashtagColor = hashtagColor,
                        likedFeeds = likedFeeds,
                        onLikeToggle = { viewModel.toggleLike(it) },
                        onCommentTap = { showCommentsForFeedId = feedId },
                        onEditFeed = { fId, regionId -> moveToEditFeed(fId, regionId) },
                        onDeleteFeed = { deleteFeed(it) },
                        onProfileClick = { userId ->
                            navController.navigate("userProfile/$userId")
                        },
                        viewModel = viewModel
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("피드를 불러올 수 없습니다.")
                    }
                }
            }
        }

        // 댓글 바텀시트
        showCommentsForFeedId?.let { fid ->
            CommentBottomSheet(
                feedId = fid,
                onDismiss = { showCommentsForFeedId = null },
                viewModel = viewModel as BoardViewModel
            )
        }
    }
}

@Composable
private fun FeedDetailContent(
    feed: FeedRecommendRes,
    hashtagColor: Color,
    likedFeeds: Set<Long>,
    onLikeToggle: (FeedRecommendRes) -> Unit,
    onCommentTap: () -> Unit,
    onEditFeed: (Long, Long) -> Unit,
    onDeleteFeed: (Long) -> Unit,
    onProfileClick: (Long) -> Unit,
    viewModel: FeedDetailViewModel
) {
    val liked = likedFeeds.contains(feed.id) || (feed.liked == true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // 상단 프로필/카테고리 + 더보기
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onProfileClick(feed.userId) }
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

            if (viewModel.userInfo?.userId == feed.userId) {
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
                                onEditFeed(feed.id, feed.regionId)
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
        }

        Spacer(Modifier.height(8.dp))

        // 본문
        Text(
            feed.content,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        // 태그
        if (!feed.tags.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.padding(horizontal = 16.dp)) {
                feed.tags.forEach { tag: TagRes ->
                    Text(
                        "#${tag.name}",
                        color = hashtagColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 이미지 영역
        if (feed.images.isNullOrEmpty()) {
            Image(
                painter = painterResource(R.drawable.pp_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { feed.images!!.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img = feed.images!![page]
                    val url = "http://i13d104.p.ssafy.io:8081${img.src}"

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    ) {
                        when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Success -> {
                                SubcomposeAsyncImageContent()
                            }
                            is coil.compose.AsyncImagePainter.State.Loading,
                            is coil.compose.AsyncImagePainter.State.Empty -> {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF6F6F6))
                                )
                            }
                            is coil.compose.AsyncImagePainter.State.Error -> {
                                Image(
                                    painter = painterResource(R.drawable.pp_logo),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // 페이지 인디케이터
                if (feed.images!!.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${feed.images!!.size}",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 좋아요 / 댓글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            IconButton(onClick = { onLikeToggle(feed) }) {
                Icon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (liked) Color(0xFFF44336) else LocalContentColor.current,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "${feed.likes}", fontSize = 16.sp)

            Spacer(Modifier.width(16.dp))

            IconButton(onClick = onCommentTap) {
                Icon(
                    painter = painterResource(R.drawable.outline_chat_bubble_24),
                    contentDescription = "댓글",
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "${feed.commentCount}", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}
