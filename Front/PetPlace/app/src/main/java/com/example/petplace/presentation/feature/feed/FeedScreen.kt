package com.example.petplace.presentation.feature.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FeedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
//    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()   // 단일 값
    val allCategories    = viewModel.allCategories
    val searchText by viewModel.searchText.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()
    var isSearchMode by remember { mutableStateOf(false) }
    var showCommentsForPostId by remember { mutableStateOf<String?>(null) }

    val backgroundColor = Color(0xFFFEF9F0)
    val hashtagTextColor = Color(0xFFF79800)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)      // start·end 한 줄로
            ) {
                viewModel.allCategories.forEach { category ->
                    val selected   = selectedCategory == category      // ✔ 단일 비교
                    val background = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFFFFDF9)
                    val content    = if (selected) Color.White else Color(0xFF374151)

                    Button(
                        onClick = { viewModel.toggleCategory(category) },
                        colors = ButtonDefaults.buttonColors(containerColor = background),
                        border = if (selected) null else ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color(0xFFFFE0B3), Color(0xFFFFE0B3)))
                        ),
                        shape  = RoundedCornerShape(14.dp),            // 모서리 축소
                        contentPadding = PaddingValues(12.dp, 4.dp),   // 내부 여백 축소
                        modifier = Modifier.padding(end = 6.dp)        // 버튼 간 간격도 소폭 축소
                    ) {
                        Text(category, color = content, fontSize = 12.sp)   // ⬅글자 크기 축소
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSearchMode) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { viewModel.updateSearchText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    placeholder = { Text("검색어를 입력하세요") },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF79800),
                        focusedBorderColor = Color(0xFFF79800),
                        cursorColor = Color(0xFFF79800),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            LazyColumn {
                items(posts) { post ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(post.profileImage),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val style = hashtagStyles[post.category] ?: Pair(Color.LightGray, Color.DarkGray)

                                Text(
                                    post.category,
                                    color = style.second,
                                    fontSize = 12.sp,
                                    modifier = Modifier.background(color = style.first, shape = RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = post.author, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = post.content, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            post.hashtags.forEach { tag ->
                                Text(text = tag, modifier = Modifier.padding(end = 4.dp), color = hashtagTextColor, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val imageCount = post.imageUrls.size

                        val pagerState = rememberPagerState(pageCount = {imageCount})

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            HorizontalPager(
                                state   = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                if (page in post.imageUrls.indices) {
                                    Image(
                                        painter = rememberAsyncImagePainter(post.imageUrls[page]),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Text(
                                text = "${pagerState.currentPage + 1}/$imageCount",
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



                        Spacer(modifier = Modifier.height(5.dp))

                        Row(modifier = Modifier.padding(horizontal = 16.dp)){
                            Icon(
                                painter = painterResource(id = R.drawable.location_marker),
                                contentDescription = "마커",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Text(text = " ${post.location}에서 작성한 글입니다.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 3.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                            var isLiked by remember { mutableStateOf(false) }

                            IconButton(onClick = { isLiked = !isLiked }) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
                                    modifier = Modifier.size(25.dp),
                                    tint = if (isLiked) Color(0xFFF44336) else LocalContentColor.current
                                )
                            }
                            Text("${post.likes}", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(15.dp))
                            IconButton(onClick = { showCommentsForPostId = post.id }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_chat_bubble_24),
                                    contentDescription = "댓글창",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            Text("${post.comments}", fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (showCommentsForPostId != null) {
            CommentBottomSheet(
                comments = viewModel.getCommentsForPost(showCommentsForPostId!!),
                onDismiss = { showCommentsForPostId = null }
            )
        }

        FloatingActionButton(
            onClick = { navController.navigate("board/write") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFFF79800),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }

        FloatingActionButton(
            onClick = { isSearchMode = !isSearchMode },
            modifier = Modifier.align(Alignment.TopEnd).offset(y = 72.dp, x = (-16).dp),
            containerColor = Color(0xFFF79800),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFEF9F0
)
@Composable
fun FeedScreenPreview(){
    FeedScreen(navController = rememberNavController())
}
