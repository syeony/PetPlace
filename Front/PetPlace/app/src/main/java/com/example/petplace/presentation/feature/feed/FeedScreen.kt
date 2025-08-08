package com.example.petplace.presentation.feature.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.example.petplace.data.model.feed.TagRes

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FeedScreen(
    navController: NavController,
    modifier:   Modifier = Modifier,
    viewModel:  BoardViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchText       by viewModel.searchText.collectAsState()
    val feeds            by viewModel.filteredFeeds.collectAsState()

    var isSearchMode          by remember { mutableStateOf(false) }
    var showCommentsForFeedId by remember { mutableStateOf<Long?>(null) }

    val bgColor      = Color(0xFFFEF9F0)
    val hashtagColor = Color(0xFFF79800)

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        // 새로고침 시작
        viewModel.refreshFeeds {
            refreshState.endRefresh()  // 새로고침 종료 시점에서 호출!
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column {
            //헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽: 로고 + 텍스트
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.pp_logo), // 업로드한 이미지 리소스
                        contentDescription = "Pet Place Logo",
                        modifier = Modifier.size(50.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Pet Place",
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B) // 짙은 남색 계열
                        )
                        Text(
                            text = "우리동네 펫 커뮤니티",
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                // 오른쪽: 검색 + 알림 아이콘
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchMode = !isSearchMode }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { /* 알림 버튼 동작 추가 가능 */ }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_notifications_24),
                            contentDescription = "알림",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            /* 검색창 (토글) */
            if (isSearchMode) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = viewModel::updateSearchText,
                    placeholder = { Text("검색어를 입력하세요", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(44.dp), // 높이 조정,
                    singleLine = true,
                    shape = RoundedCornerShape(45.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = hashtagColor,
                        unfocusedBorderColor = hashtagColor,
                        cursorColor          = hashtagColor,
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            /* 카테고리 선택 바 */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                viewModel.allCategories.forEach { cat ->
                    val picked   = selectedCategory == cat
                    val bg       = if (picked) MaterialTheme.colorScheme.primary else Color(0xFFFFFDF9)
                    val txtColor = if (picked) Color.White else Color(0xFF374151)

                    Button(
                        onClick = { viewModel.toggleCategory(cat) },
                        colors = ButtonDefaults.buttonColors(containerColor = bg),
                        border = if (picked) null else ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color(0xFFFFE0B3), Color(0xFFFFE0B3)))
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(cat, color = txtColor, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            /* 피드 리스트 */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(refreshState.nestedScrollConnection)
            ) {
                LazyColumn {
                    items(feeds) { feed ->
                        FeedItem(
                            feed = feed,
                            hashtagColor = hashtagColor,
                            onCommentTap = { showCommentsForFeedId = feed.id },
                            viewModel = viewModel
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
                // 이건 그거임. 스크롤 땡기면 업데이트되는거.
                if (refreshState.isRefreshing || refreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = refreshState,
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }

        /* 댓글 바텀시트 */
        showCommentsForFeedId?.let { fid ->
            CommentBottomSheet(
                feedId = fid,
                comments = viewModel.getCommentsForFeed(fid),
                onDismiss = { showCommentsForFeedId = null },
                viewModel = viewModel
            )
        }

        /* 글쓰기 + 검색 FAB */
        FloatingActionButton(
            onClick = { navController.navigate("board/write") },
            containerColor = Color(0xFFF79800),
            contentColor   = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "글쓰기") }
    }
}

//피드
@Composable
private fun FeedItem(
    feed: FeedRecommendRes,
    hashtagColor:  Color,
    onCommentTap:  () -> Unit,
    viewModel: BoardViewModel
) {
    val liked = viewModel.isFeedLiked(feed.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        /* 프로필 & 카테고리 */
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

        /* 본문 */
        Text(feed.content, modifier = Modifier.padding(horizontal = 16.dp))

        /* 태그 */
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
        Spacer(Modifier.height(8.dp))

        /* ───── 이미지 영역 ───── */
        if (feed.images.isNullOrEmpty()) {
            // 이미지가 없으면
            Image(
                painter        = painterResource(R.drawable.pp_logo),
                contentDescription = null,
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale   = ContentScale.Crop
            )
        } else {
            // 이미지가 있으면 Pager + 인디케이터
            val pagerState = rememberPagerState(pageCount = { feed.images!!.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                HorizontalPager(
                    state   = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val img: ImageRes = feed.images!![page]
                    Image(
                        painter = rememberAsyncImagePainter("http://i13d104.p.ssafy.io:8081"+img.src),
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

        /* 좋아요 / 댓글 */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
//            var liked by remember { mutableStateOf(false) }

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

            IconButton(onClick = onCommentTap) {
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