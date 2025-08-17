package com.example.petplace.presentation.feature.hotel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

/* ───────── 이미지 경로 전처리(이 파일 전용) ───────── */
private const val IMAGE_BASE_URL = "http://i13d104.p.ssafy.io:8081/"  // ← 실제 서버 도메인으로 변경

private fun resolveImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val t = raw.trim()
    return if (t.startsWith("http://") || t.startsWith("https://")) t
    else IMAGE_BASE_URL.trimEnd('/') + "/" + t.trimStart('/')
}
/* ─────────────────────────────────────────────────── */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel = hiltViewModel()
) {
    // 상태
    val reservation by viewModel.reservationState.collectAsState()
    val detail by viewModel.hotelDetail.collectAsState()
    val error by viewModel.error.collectAsState()

    // 진입 시 상세 로드
    LaunchedEffect(reservation.selectedHotelId) {
        reservation.selectedHotelId?.let { viewModel.getHotelDetail() }
    }
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isAvailable) {
        if (uiState.isAvailable == true) {
            navController.navigate("hotel/checkout")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("호텔 상세", style = AppTypography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,        // 바텀바 배경 화이트
                tonalElevation = 0.dp,      // 톤 오버레이 제거
                shadowElevation = 4.dp      // 분리감은 그림자로
            ) {
                Button(
                    onClick = { viewModel.checkReservationAvailability() },
                    enabled = detail != null, // 로딩 중엔 비활성
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (detail != null) "예약하기" else "불러오는 중…")
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            // 에러 표시
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 상단 이미지 (URL 전처리 후 로드)
            Card(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(resolveImageUrl(detail?.imageUrl)) // ✅ 상대경로도 절대 URL로
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.pp_logo),
                    error = painterResource(R.drawable.pp_logo),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 이름/주소
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = detail?.name ?: "로딩 중…",
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = detail?.address ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // 설명 (description) — 더보기/접기
            if (!detail?.description.isNullOrBlank()) {
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "설명",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF333333)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = detail!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555),
                            maxLines = if (expanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (detail!!.description.length > 60) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = { expanded = !expanded },
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (expanded) "접기" else "더보기")
                            }
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // 지도
            Card(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                val lat = detail?.latitude
                val lng = detail?.longitude
                val title = detail?.name ?: ""
                if (lat != null && lng != null) {
                    HotelKakaoMap(latitude = lat, longitude = lng, placeName = title)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 하단 버튼 영역과 겹치지 않게 여백
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun HotelKakaoMap(
    latitude: Double,
    longitude: Double,
    placeName: String
) {
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val kakaoMapState = remember { mutableStateOf<KakaoMap?>(null) }
    val stylePoi by remember {
        mutableStateOf(LabelStyles.from(LabelStyle.from(R.drawable.marker_resized_48x48)))
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewState.value = this
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(e: Exception?) { e?.printStackTrace() }
                        override fun onMapResumed() {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMapState.value = map
                            val pos = LatLng.from(latitude, longitude)
                            map.labelManager?.layer?.addLabel(
                                LabelOptions.from(pos).setStyles(stylePoi)
                            )
                            map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 16))
                        }
                    }
                )
            }
        }
    )
}
