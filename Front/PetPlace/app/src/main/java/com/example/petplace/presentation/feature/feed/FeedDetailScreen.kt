package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.petplace.R
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.feed.TagRes
import kotlinx.coroutines.launch

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
            FeedDetailCommentBottomSheet(
                feedId = fid,
                onDismiss = { showCommentsForFeedId = null },
                viewModel = viewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailCommentBottomSheet(
    feedId: Long,
    onDismiss: () -> Unit,
    viewModel: FeedDetailViewModel
) {
    // 1) 댓글 리스트 구독
    val comments by viewModel.commentList.collectAsState()

    // 2) BottomSheet 열릴 때마다 API 호출
    LaunchedEffect(feedId) {
        viewModel.refreshComments(feedId)
    }

    val topLevelComments = comments.filter { it.parentId == null }

    // 댓글 개수에 따라 즉시 초기화
    val expandedStates = remember(topLevelComments.size) {
        mutableStateListOf<Boolean>().apply {
            repeat(topLevelComments.size) { add(false) }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // 입력창 & 포커싱
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var replyingTo by remember { mutableStateOf<Long?>(null) }

    // 삭제 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCommentId by remember { mutableStateOf<Long?>(null) }

    // 삭제 확인 Dialog
    if (showDeleteDialog && selectedCommentId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("댓글 삭제") },
            text = { Text("정말 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.removeComment(selectedCommentId!!, feedId)
                        showDeleteDialog = false
                        selectedCommentId = null
                    }
                }) { Text("삭제") }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    selectedCommentId = null
                }) { Text("취소") }
            }
        )
    }

    // 시트 자동 표시
    LaunchedEffect(Unit) { sheetState.show() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding(),
            bottomBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                if (replyingTo == null) "댓글을 입력하세요"
                                else "답글을 입력하세요"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commentText.isNotBlank()) {
                                    coroutineScope.launch {
                                        // 1) 댓글 등록
                                        viewModel.addComment(feedId, replyingTo, commentText)
                                        // 2) 바로 펼치기
                                        replyingTo?.let { parentId ->
                                            val idx = topLevelComments.indexOfFirst { it.id == parentId }
                                            if (idx >= 0) expandedStates[idx] = true
                                        }
                                        // 3) 초기화
                                        commentText = ""
                                        replyingTo = null
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.paper_airplane),
                        contentDescription = "전송",
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {
                                if (commentText.isNotBlank()) {
                                    coroutineScope.launch {
                                        viewModel.addComment(feedId, replyingTo, commentText)
                                        replyingTo?.let { parentId ->
                                            val idx = topLevelComments.indexOfFirst { it.id == parentId }
                                            if (idx >= 0) expandedStates[idx] = true
                                        }
                                        commentText = ""
                                        replyingTo = null
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                        tint = Color.Unspecified
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    "댓글",
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                LazyColumn {
                    items(topLevelComments.size) { idx ->
                        val comment = topLevelComments[idx]
                        val isMine = comment.userId == viewModel.userInfo?.userId

                        Column(Modifier.padding(vertical = 8.dp)) {
                            // 최상위 댓글 Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .then(
                                        if (isMine) Modifier.pointerInput(comment.id) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    selectedCommentId = comment.id
                                                    showDeleteDialog = true
                                                }
                                            )
                                        } else Modifier
                                    )
                            ) {
                                ProfileImage(comment.userImg)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(comment.userNick, fontWeight = FontWeight.Bold)
                                    Text(comment.content)
                                }
                            }

                            // 답글 달기 버튼
                            TextButton(onClick = {
                                commentText = ""
                                replyingTo = comment.id
                                // 포커스 + 키보드 강제
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }) { Text("답글 달기", fontSize = 10.sp) }

                            // 대댓글 영역
                            if (comment.replies?.isNotEmpty() == true) {
                                TextButton(onClick = {
                                    expandedStates[idx] = !expandedStates[idx]
                                }) {
                                    Text(
                                        if (expandedStates[idx]) "          답글 숨기기"
                                        else                     "            답글 더 보기",
                                        fontSize = 10.sp
                                    )
                                }
                                if (expandedStates[idx]) {
                                    comment.replies?.forEach { reply ->
                                        val isReplyMine = reply.userId == viewModel.userInfo?.userId
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(start = 36.dp, top = 4.dp, bottom = 20.dp)
                                                .then(
                                                    if (isReplyMine) Modifier.pointerInput(reply.id) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                selectedCommentId = reply.id
                                                                showDeleteDialog = true
                                                            }
                                                        )
                                                    } else Modifier
                                                )
                                        ) {
                                            ProfileImage(reply.userImg)
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(reply.userNick, fontWeight = FontWeight.Bold)
                                                Text(reply.content)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}