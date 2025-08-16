package com.example.petplace.presentation.feature.missing_report

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.data.model.missing_report.MissingReportDetailDto
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundColor
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// Kakao Vector Map
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

/* ---- 상대 경로를 절대 URL로 변환하는 헬퍼 ---- */
private const val BASE_URL = "http://i13d104.p.ssafy.io:8081"
private fun String?.toFullUrlOrNull(): String? =
    if (this.isNullOrBlank()) null else if (startsWith("http")) this else BASE_URL + this

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingReportDetailScreen(
    missingReportId: Long,
    navController: NavController,
    vm: MissingReportDetailViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(missingReportId) { vm.load(missingReportId) }

    LaunchedEffect(ui.createdChatRoomId) {
        ui.createdChatRoomId?.let { chatRoomId ->
            navController.navigate("chatDetail/$chatRoomId")
            vm.consumeCreatedChatRoomId()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("목격 제보 상세", fontWeight = FontWeight.Bold)
                    }
                     },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,          // ✅ 탑바 배경 화이트
                    scrolledContainerColor = Color.White,  // ✅ 스크롤 시에도 화이트 유지
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            ui.data?.let { detail ->
                // 현재 사용자 ID 가져오기
                val app = LocalContext.current.applicationContext as PetPlaceApp
                val currentUserId = app.getUserInfo()?.userId ?: 0

                // 본인 게시글이 아닐 때만 버튼 표시
                if (detail.userId != currentUserId) {
                    Surface(
                        color = Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Button(
                            onClick = { vm.startChatWithUser(detail.userId) },
                            enabled = !ui.isChatRoomCreating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF79800),
                                contentColor = Color.White
                            )
                        ) {
                            if (ui.isChatRoomCreating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("생성 중...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("채팅하기", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    ) { inner ->
        val topPadding = inner.calculateTopPadding()

        when {
            ui.loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            ui.error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("불러오기 실패: ${ui.error}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.load(missingReportId) }) { Text("다시 시도") }
                }
            }

            ui.data != null -> DetailContent(
                detail = ui.data!!,
                modifier = Modifier.padding(top = topPadding)
            )
        }
    }
}

@Composable
private fun DetailContent(detail: MissingReportDetailDto, modifier: Modifier = Modifier) {
    var mainIndex by remember { mutableStateOf(0) }
    val fmt = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm") }
    fun f(iso: String) = runCatching { OffsetDateTime.parse(iso).format(fmt) }.getOrElse { iso }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 84.dp) // 바텀 버튼과 겹침 방지
    ) {
        // 메인 이미지 + 썸네일
        // 메인 이미지 + 썸네일
        item {
            val main = detail.images.getOrNull(mainIndex)?.src.toFullUrlOrNull()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)                  // ← 여기서 고정! (필요하면 16f/9f 등으로 변경)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = main,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop       // ← 중앙 크롭 (레이아웃 점프 없음)
                )
            }

            Spacer(Modifier.height(10.dp))

            if (detail.images.size > 1) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detail.images) { img ->
                        val idx = detail.images.indexOf(img)
                        AsyncImage(
                            model = img.src.toFullUrlOrNull(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    if (idx == mainIndex) 2.dp else 1.dp,
                                    if (idx == mainIndex) MaterialTheme.colorScheme.primary else Color(
                                        0xFFDADADA
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { mainIndex = idx },
                            contentScale = ContentScale.Crop     // 썸네일은 그대로 크롭
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }


        // 📍 카카오 맵 (목격 위치)
        item {
            Spacer(Modifier.height(12.dp))
            Text("목격 위치", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            val lat = detail.latitude
            val lng = detail.longitude

            if (lat != null && lng != null) {
                KakaoMapBox(lat = lat, lng = lng)
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(12.dp)),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("좌표 정보가 없어 지도를 표시할 수 없습니다.")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 작성자 & 메타
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val userImg = (detail.userImg ?: "").toFullUrlOrNull()
                    ?: "https://picsum.photos/seed/user_${detail.userId}/120/120"
                AsyncImage(
                    model = userImg,
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(detail.userNickname, fontWeight = FontWeight.SemiBold)
                    Text(
                        "등록 ${f(detail.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaRow("지역", detail.regionName)
                    MetaRow("주소", detail.address)
                    MetaRow("목격 시각", f(detail.sightedAt))
                    MetaRow("품종(추정)", detail.breed ?: "미상")
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 내용
        item {
            Text("설명", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = detail.content.ifBlank { "작성된 설명이 없습니다." },
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF212121)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, maxLines = 1)
    }
}

@Composable
fun KakaoMapBox(
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
        .clip(RoundedCornerShape(12.dp))
        .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(12.dp))
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    val style by remember {
        mutableStateOf(LabelStyles.from(LabelStyle.from(R.drawable.marker_resized_48x48)))
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )

    LaunchedEffect(Unit) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {}
                override fun onMapError(e: Exception?) { e?.printStackTrace() }
                override fun onMapResumed() {}
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    kakaoMap = map
                    val pos = LatLng.from(lat, lng)
                    map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
                    val layer = map.labelManager?.layer ?: return
                    layer.removeAll()
                    layer.addLabel(LabelOptions.from(pos).setStyles(style))
                }
            }
        )
    }

    LaunchedEffect(lat, lng) {
        val map = kakaoMap ?: return@LaunchedEffect
        val layer = map.labelManager?.layer ?: return@LaunchedEffect
        val pos = LatLng.from(lat, lng)
        layer.removeAll()
        layer.addLabel(LabelOptions.from(pos).setStyles(style))
        map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
    }
}
