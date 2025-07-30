package com.example.petplace.presentation.feature.board

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BoardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()
    var isSearchMode by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFFEF9F0)
    val hashtagTextColor = Color(0xFFF79800)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = modifier.padding(top = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                viewModel.allCategories.forEach { category ->
                    val selected = selectedCategories.contains(category)
                    val background = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFFFFDF9)
                    val content = if (selected) Color.White else Color(0xFF374151)
                    val border = if (selected) null else ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFE0B3), Color(0xFFFFE0B3)))
                    )
                    Button(
                        onClick = { viewModel.toggleCategory(category) },
                        colors = ButtonDefaults.buttonColors(containerColor = background),
                        border = border,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = category, color = content)
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
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(post.profileImage),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val style = hashtagStyles[post.category] ?: Pair(Color.LightGray, Color.DarkGray)
                                Text(
                                    post.category,
                                    color = style.second,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .background(color = style.first, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(text = post.author, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = post.content,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                        ) {
                            post.hashtags.forEach { tag ->
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(end = 4.dp),
                                    color = hashtagTextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(post.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "📍 ${post.location}에서 작성한 글입니다.", fontSize = 12.sp, color = Color.Gray,
                            modifier = Modifier
                                .padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)) {
                            Text("❤️ ${post.likes}", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("💬 ${post.comments}", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // 오른쪽 하단 글쓰기 플로팅 버튼
        FloatingActionButton(
            onClick = { navController.navigate("board/write") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFFF79800),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }

        // 검색 플로팅 버튼 (카테고리 아래, 게시글과 겹치게)
        FloatingActionButton(
            onClick = { isSearchMode = !isSearchMode },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 72.dp, x = (-16).dp),
            containerColor = Color(0xFFF79800),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}