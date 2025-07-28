package com.example.petplace.presentation.feature.board

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val search = viewModel.searchText
    val posts = viewModel.postList
    val tags = viewModel.tags

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* 글쓰기 이동 */ },
                icon = { Icon(Icons.Default.Add, contentDescription = "글쓰기") },
                text = { Text("글쓰기") },
                containerColor = Color(0xFFFFA500),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 0.dp)
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp) // innerPadding 제거
        ) {
            Text("💡 구미시 인의동", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { viewModel.updateSearchText(it) },
                placeholder = { Text("게시물 검색...") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.applySearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFFFFA500)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                tags.forEach {
                    AssistChip(
                        onClick = { viewModel.filterByTag(it) },
                        label = { Text(it) },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { post ->
                    PostCard(post)
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFFCF9),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                val style = hashtagStyles[post.category] ?: Pair(Color.LightGray, Color.DarkGray)

                Text(
                    post.category,
                    color = style.second,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(color = style.first, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(post.title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(post.body, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(post.meta, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(){
                Image(
                    painter = painterResource(id = post.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Row(

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat),
                        contentDescription = "댓글",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentCount}", fontSize = 12.sp, color = Color.Gray)
                }
            }

        }
    }
}

data class Post(
    val category: String,
    val title: String,
    val body: String,
    val meta: String,
    val commentCount: Int,
    val imageRes: Int
)
