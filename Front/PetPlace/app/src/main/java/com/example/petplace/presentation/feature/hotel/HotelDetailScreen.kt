package com.example.petplace.presentation.feature.hotel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.R
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

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
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Button(
                    onClick = {
                        viewModel.checkReservationAvailability()
                    },
                    enabled = detail != null,              // 로딩 중엔 비활성
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
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

            // 상단 이미지 (로딩 중엔 로고)
            Card(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                AsyncImage(
                    model = detail?.imageUrl,
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
