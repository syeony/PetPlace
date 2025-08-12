package com.example.petplace.presentation.feature.walk_and_care

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.petplace.presentation.feature.feed.categoryStyles

@Composable
fun WalkPostDetailScreen(
    navController: NavController,
    category: String,
    title: String,
    body: String,
    date: String,
    time: String,
    imageUrl: String,
    reporterName: String,
    reporterAvatarUrl: String? = null
) {
    val orange = Color(0xFFF79800)
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            }
        },
        bottomBar = {
            // 하단 고정 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // 뒤에 내용 비치지 않게
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { /* TODO: 채팅 진입 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orange)
                ) {
                    Text("채팅하기", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->

        // 스크롤 되는 본문
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = 0.dp,
                bottom = 88.dp // 하단 고정 버튼 높이만큼 여유(버튼에 가리지 않게)
            )
        ) {
            item {
                // ===== 헤더: 프로필 + (칩/이름) =====
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            reporterAvatarUrl ?: "https://via.placeholder.com/80x80.png?text=PP"
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEAEAEA), RoundedCornerShape(999.dp))
                            .clip(RoundedCornerShape(999.dp))
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val style = categoryStyles[category] ?: (Color(0xFFFFF4E5) to Color(0xFF8A5800))
                        Text(
                            text = category,
                            color = style.second,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(style.first, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = reporterName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = body,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = orange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("날짜", fontSize = 13.sp, color = Color(0xFF6B7280))
                        Spacer(Modifier.width(10.dp))
                        Text(date, fontSize = 13.5.sp, color = Color(0xFF111827))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = orange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("시간", fontSize = 13.sp, color = Color(0xFF6B7280))
                        Spacer(Modifier.width(10.dp))
                        Text(time.ifBlank { "-" }, fontSize = 13.5.sp, color = Color(0xFF111827))
                    }
                }
            }

            // 내용이 아주 길어져도 스크롤 잘 되도록 여유 여백
            item { Spacer(Modifier.height(6.dp)) }
        }
    }
}
