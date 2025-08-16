package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCommentScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyCommentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.loadMyComments()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 앱바
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "내 댓글",
                    style = AppTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White
            )
        )

        // 로딩 상태
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // 에러 상태
        uiState.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "댓글을 불러오는데 실패했습니다",
                        style = AppTypography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.loadMyComments() }) {
                        Text("다시 시도")
                    }
                }
            }
            return
        }

        // 댓글 목록
        if (uiState.comments.isEmpty()) {
            // 빈 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.my_comment),
                        contentDescription = "댓글 없음",
                        modifier = Modifier.size(64.dp),
                        alpha = 0.5f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "작성한 댓글이 없습니다",
                        style = AppTypography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // 댓글 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onClick = {
                            // 원본 게시글로 이동하는 로직
                            // 게시글 상세 화면으로 네비게이션
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: MyCommentInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 댓글 작성자 정보 (본인)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    val profileImageUrl = if (!comment.authorProfileImage.isNullOrEmpty()) {
                        if (comment.authorProfileImage.startsWith("http")) {
                            comment.authorProfileImage
                        } else {
                            "http://43.201.108.195:8081${comment.authorProfileImage}"
                        }
                    } else null
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "프로필 이미지",
                        placeholder = painterResource(id = R.drawable.ic_mypage),
                        error = painterResource(id = R.drawable.ic_mypage),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = comment.authorName,
                        style = AppTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )
//                    Text(
//                        text = comment.region,
//                        style = AppTypography.bodySmall,
//                        color = Color.Gray
//                    )
                }

                Text(
                    text = comment.timeAgo,
                    style = AppTypography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 댓글 내용
            Text(
                text = comment.content,
                style = AppTypography.bodyMedium,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 구분선
            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 원본 게시글 정보
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    text = "원본 게시글:",
//                    style = AppTypography.bodySmall,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = comment.originalPostTitle,
//                    style = AppTypography.bodySmall.copy(
//                        fontWeight = FontWeight.Medium
//                    ),
//                    color = Color(0xFF1976D2),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.weight(1f)
//                )
//            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCommentScreenPreview() {
    MyCommentScreen(navController = rememberNavController())
}