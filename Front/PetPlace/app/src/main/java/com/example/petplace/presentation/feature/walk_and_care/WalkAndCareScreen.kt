package com.example.petplace.presentation.feature.walk_and_care

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.presentation.feature.feed.categoryStyles
import androidx.hilt.navigation.compose.hiltViewModel // ✅ 이거 추가
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

private const val BASE = "http://i13d104.p.ssafy.io:8081"


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalkAndCareScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WalkAndCareViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val search = viewModel.searchText.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()

    val hashtagColor = Color(0xFFFFE0B3)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("walk_write") },   // ✅ 이동
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
//                .background(Color.White)
        ) {
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
                        .height(48.dp), // 높이 조정,
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
                                        .height(30.dp) // 버튼 높이 고정
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
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(posts) { post ->
                    PostCard(post = post) {
                        navController.navigateToWalkDetail(post)
                    }
                }
            }
        }
    }
}

private fun String.e() = Uri.encode(this)

fun NavController.navigateToWalkDetail(post: Post) {
    val route =
        "walk_detail?" +
                "category=${post.category.e()}" +
                "&title=${post.title.e()}" +
                "&body=${post.body.e()}" +
                "&date=${post.date.e()}" +
                "&time=${post.time.e()}" +
                "&imageUrl=${post.imageUrl.e()}" +
                "&name=${post.reporterName.e()}" +
                "&avatar=${(post.reporterAvatarUrl ?: "").e()}"

    navigate(route)
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    val orange  = Color(0xFFF79800)
    Surface(
        color = Color(0xFFFFFCF9),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column() {
                    InfoRow(
                        icon = Icons.Default.DateRange,
                        iconTint = orange,
                        label = "날짜",
                        value = post.date
                    )
                    Spacer(Modifier.height(2.dp))
                    InfoRow(
                        icon = Icons.Default.Info,
                        iconTint = orange,
                        label = "시간",
                        value = post.time
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // PostCard의 이미지 렌더러 교체
            Image(
                painter = rememberAsyncImagePainter(
                    fullUrl(post.imageUrl).also {
                        Log.d("WalkAndCareScreen", "이미지 URL: $it")
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.Bottom)
            )




        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 13.sp)   // 회색 라벨
        Spacer(Modifier.width(8.dp))
        Text(value, color = Color.Gray, fontSize = 13.5.sp)
    }
}


private fun resolveImageUrl(raw: String?): String? {
    if (raw == null) return null

    // 1) 흔한 쓰레기 문자 정리
    var s = raw.trim()
        .removePrefix("\"").removeSuffix("\"")     // 양끝 따옴표 제거
        .removePrefix("[").removeSuffix("]")       // 배열 문자열로 올 때
    if (s.isBlank() || s.equals("null", true)) return null

    // 2) 여러 개가 콤마/세미콜론으로 올 때 첫 것만
    s = s.split(',', ';').first().trim()

    // 3) 이미 풀 URL이면 그대로
    if (s.startsWith("http://") || s.startsWith("https://")) return s

    // 4) BASE + 경로를 안전하게 합치기 (인코딩 포함)
    //    Uri.Builder가 경로 세그먼트를 알아서 인코딩해줌
    val base = android.net.Uri.parse(BASE)
    val clean = s.removePrefix("/") // 절대경로면 슬래시 제거하고 세그먼트로 추가
    val segments = clean.split('/').filter { it.isNotBlank() }

    val builder = base.buildUpon().encodedPath(null) // 기존 path 초기화
    segments.forEach { seg -> builder.appendPath(seg) } // 각 세그먼트 인코딩

    return builder.build().toString() // 예: http://.../images/1755071....jpg
}// 공용: null/빈값/슬래시 정리
private fun fullUrl(path: String?): Any {
    val p = path?.trim().orEmpty()
    if (p.isBlank() || p.equals("null", true)) return R.drawable.pp_logo
    return if (p.startsWith("http")) p else BASE + (if (p.startsWith("/")) "" else "/") + p
}