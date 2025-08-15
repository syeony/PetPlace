package com.example.petplace.presentation.feature.walk_and_care

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.presentation.feature.feed.categoryStyles

private const val BASE = "http://i13d104.p.ssafy.io:8081"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WalkAndCareScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WalkAndCareViewModel = hiltViewModel(),
//    currentLat: Double? = null,
//    currentLon: Double? = null
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val search = viewModel.searchText.collectAsState()
    val posts by viewModel.filteredPosts.collectAsState()
    val regionName by viewModel.regionName.collectAsState()

    val context = LocalContext.current

    // 위치 권한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* no-op */ }

    // 최초 진입 시 권한 요청
    LaunchedEffect(Unit) {
        val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val pm = ContextCompat.checkSelfPermission(context, fine)
        val pm2 = ContextCompat.checkSelfPermission(context, coarse)
        if (pm != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            pm2 != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(arrayOf(fine, coarse))
        }
    }

//    // 권한 있으면 현재 위치 가져와서 region 인증
//    LaunchedEffect(regionName) {
//        if (regionName != null) return@LaunchedEffect
//        val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
//        val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
//        val hasFine = ContextCompat.checkSelfPermission(context, fine) ==
//                android.content.pm.PackageManager.PERMISSION_GRANTED
//        val hasCoarse = ContextCompat.checkSelfPermission(context, coarse) ==
//                android.content.pm.PackageManager.PERMISSION_GRANTED
//
//        if (hasFine || hasCoarse) {
//            val loc = LocationProvider.getCurrentLocation(context)
//            if (loc != null) {
//                Log.d("WalkAndCareScreen", "GPS lat=${loc.latitude}, lon=${loc.longitude}")
//                viewModel.setRegionByLocation(loc.latitude, loc.longitude)
//            } else {
//                Log.e("WalkAndCareScreen", "현재 위치를 가져오지 못했습니다 (null)")
//            }
//        }
//    }

//    // 외부에서 좌표 주입 시
//    LaunchedEffect(currentLat, currentLon) {
//        if (currentLat != null && currentLon != null) {
//            viewModel.setRegionByLocation(currentLat, currentLon)
//        }
//    }

    val hashtagColor = Color(0xFFFFE0B3)

    // 작성 성공 감지 → 새로고침
    val handle = navController.currentBackStackEntry?.savedStateHandle
    val postCreated by remember(handle) {
        handle?.getStateFlow("walk_post_created", false)
    }?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(postCreated) {
        if (postCreated) {
            viewModel.fetchPosts()
            handle?.remove<Boolean>("walk_post_created")
            handle?.remove<Long>("walk_post_id")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("walk_write") },
                containerColor = Color(0xFFF79800),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 0.dp)
                    .offset(y = 12.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "글쓰기") }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_marker),
                        contentDescription = "위치",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = regionName ?: " 동네 확인 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = search.value,
                    onValueChange = viewModel::updateSearchText,
                    placeholder = { Text("찾으시는 단어를 입력하세요", fontSize = 12.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = hashtagColor,
                        unfocusedBorderColor = hashtagColor,
                        cursorColor = hashtagColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 카테고리 필터: enum 라벨 기준(산책구인/산책의뢰/돌봄구인/돌봄의뢰)
                val columns = 4
                Column(modifier = Modifier.fillMaxWidth()) {
                    viewModel.allCategories.chunked(columns).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { cat ->
                                val picked = selectedCategory == cat
                                val bg = if (picked) MaterialTheme.colorScheme.primary else Color(0xFFFFFDF9)
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
                                        .height(30.dp)
                                ) {
                                    Text(cat, color = txtColor, fontSize = 12.sp)
                                }
                            }
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
    navigate("walk_detail/${post.id}")
}

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    val orange = Color(0xFFF79800)
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
                    post.category, // 이미 라벨형(산책구인/산책의뢰/돌봄구인/돌봄의뢰)
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
                Column {
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
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Text(value, color = Color.Gray, fontSize = 13.5.sp)
    }
}

private fun resolveImageUrl(raw: String?): String? {
    if (raw == null) return null
    var s = raw.trim()
        .removePrefix("\"").removeSuffix("\"")
        .removePrefix("[").removeSuffix("]")
    if (s.isBlank() || s.equals("null", true)) return null
    s = s.split(',', ';').first().trim()
    if (s.startsWith("http://") || s.startsWith("https://")) return s

    val base = android.net.Uri.parse(BASE)
    val clean = s.removePrefix("/")
    val segments = clean.split('/').filter { it.isNotBlank() }

    val builder = base.buildUpon().encodedPath(null)
    segments.forEach { seg -> builder.appendPath(seg) }
    return builder.build().toString()
}

private fun fullUrl(path: String?): Any {
    val p = path?.trim().orEmpty()
    if (p.isBlank() || p.equals("null", true)) return R.drawable.pp_logo
    return if (p.startsWith("http")) p else BASE + (if (p.startsWith("/")) "" else "/") + p
}
