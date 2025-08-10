package com.example.petplace.presentation.feature.walk_and_care

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.presentation.feature.feed.categoryStyles

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalkAndCareScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WalkAndCareViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val search = viewModel.searchText.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()

    val hashtagColor = Color(0xFFF79800)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {  },
                containerColor = Color(0xFFF79800),
                contentColor   = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 0.dp) // ↓ 바닥 여백 줄이고
                    .offset(y = 12.dp)                    // ↓ 화면 하단으로 더 내리기
            ) { Icon(Icons.Default.Add, contentDescription = "글쓰기") }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            // ⬇️ 헤더(위치, 검색, 카테고리)만 패딩 유지
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ){
                Row(modifier = Modifier
                    .padding(start = 8.dp)){
                    Icon(
                        painter = painterResource(id = R.drawable.location_marker),
                        contentDescription = "위치",
                        modifier = Modifier
                            .size(16.dp), // 원하는 크기로 조절
                        tint = Color.Unspecified // 원본 이미지 색상 유지
                    )
                    Text(" 구미시 인의동", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = search.value,
                    onValueChange = viewModel::updateSearchText,
                    placeholder = { Text("찾으시는 단어를 입력하세요", fontSize = 12.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 30.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = hashtagColor,
                        unfocusedBorderColor = hashtagColor,
                        cursorColor          = hashtagColor,
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val columns = 4  // 한 줄에 2개 또는 3개 원하시는 개수 설정

                Column(modifier = Modifier.fillMaxWidth()) {
                    viewModel.allCategories.chunked(columns).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { cat ->
                                val picked   = selectedCategory == cat
                                val bg       = if (picked) MaterialTheme.colorScheme.primary else Color(0xFFFFFDF9)
                                val txtColor = if (picked) Color.White else Color(0xFF374151)

                                Button(
                                    onClick = { viewModel.toggleCategory(cat) },
                                    colors = ButtonDefaults.buttonColors(containerColor = bg),
                                    border = if (picked) null else ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(listOf(hashtagColor, hashtagColor))
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp) // 버튼 높이 고정
                                ) {
                                    Text(cat, color = txtColor, fontSize = 12.sp)
                                }
                            }

                            // 빈 공간 채우기 (아이템 수가 columns보다 적을 경우)
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(vertical = 6.dp),
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
        color = Color(0xFFFFFCF9),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                val style = categoryStyles[post.category] ?: Pair(Color.LightGray, Color.DarkGray)

                Text(
                    post.category,
                    color = style.second,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(color = style.first, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(post.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = post.body,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,                           // ✅ 본문 2줄
                    overflow = TextOverflow.Ellipsis        // ✅ 말줄임
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(post.meta, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(id = post.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

