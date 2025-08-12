package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodViewModel
import com.example.petplace.util.CommonUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: NeighborhoodViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // 위치 조회
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(Unit) {
        CommonUtils.getXY(context)?.let { (lat, lon) ->
            currentLat = lat
            currentLng = lon
        }
    }

    // MapView / KakaoMap 상태
    val mapViewState = remember { mutableStateOf<MapView?>(null) }
    val kakaoMapState = remember { mutableStateOf<KakaoMap?>(null) }

    // 마커들
    val markers by viewModel.markers.collectAsState()

    // 라벨 스타일 (현재 위치 + 마커 공통)
    val locationStyle by remember {
        mutableStateOf(LabelStyles.from(LabelStyle.from(R.drawable.location_on)))
    }

    // 중앙 고정 핀의 좌표 & 주소
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var addressText by remember { mutableStateOf("주소를 불러오는 중...") }

    // selectedLatLng 변경될 때마다 역지오코딩
    LaunchedEffect(selectedLatLng) {
        val c = selectedLatLng ?: return@LaunchedEffect
        addressText = geocodeAddress(context, c.latitude, c.longitude) ?: "주소를 불러올 수 없어요"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("지도에서 위치 확인", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = addressText,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val latLng = selectedLatLng ?: return@Button
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("selected_address", addressText)
                            set("selected_lat",    latLng.latitude)
                            set("selected_lng",    latLng.longitude)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = "이 위치로 설정", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentLat != null && currentLng != null) {
                // 지도 뷰
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapViewState.value = this
                            start(
                                object : MapLifeCycleCallback() {
                                    override fun onMapDestroy() {}
                                    override fun onMapError(e: Exception?) {
                                        Log.e("MissingMapScreen", "Map error", e)
                                    }
                                    override fun onMapResumed() {}
                                },
                                object : KakaoMapReadyCallback() {
                                    override fun onMapReady(map: KakaoMap) {
                                        kakaoMapState.value = map

                                        val pos = LatLng.from(currentLat!!, currentLng!!)
                                        // 현재 위치 라벨
                                        map.labelManager?.layer?.addLabel(
                                            LabelOptions.from(pos).setStyles(locationStyle)
                                        )
                                        // 카메라 이동
                                        map.moveCamera(
                                            CameraUpdateFactory.newCenterPosition(pos, 15)
                                        )

                                        // 최초 주소 세팅
                                        selectedLatLng = pos

                                        // 카메라 이동 종료 리스너: 중앙 좌표를 선택 좌표로 반영
                                        map.setOnCameraMoveEndListener { _, cameraPosition, _ ->
                                            val center = cameraPosition.position
                                            selectedLatLng = center
                                        }
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 중앙 고정 핀 (지도 위 오버레이)
                Image(
                    painter = painterResource(id = R.drawable.location_on),
                    contentDescription = "선택 핀",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                )

                // 안내 토스트 박스
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("지도를 움직여 위치를 설정하세요.", fontSize = 14.sp)
                }

                // "현재 위치로 이동" FAB
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .shadow(6.dp, CircleShape, clip = false)
                        .background(color = Color.White, shape = CircleShape)
                        .clickable(
                            onClick = {
                                val map = kakaoMapState.value ?: return@clickable
                                val lat = currentLat ?: return@clickable
                                val lng = currentLng ?: return@clickable
                                val pos = LatLng.from(lat, lng)
                                map.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15))
                            },
                            role = Role.Button
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.my_location_24px),
                        contentDescription = "현재 위치로 이동",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 마커 갱신 (현재 위치 라벨 + 서버/VM 마커들)
                LaunchedEffect(markers, currentLat, currentLng) {
                    val map = kakaoMapState.value ?: return@LaunchedEffect
                    val layer = map.labelManager?.layer ?: return@LaunchedEffect

                    val curLat = currentLat ?: return@LaunchedEffect
                    val curLng = currentLng ?: return@LaunchedEffect
                    val currentPos = LatLng.from(curLat, curLng)

                    layer.removeAll()
                    layer.addLabel(LabelOptions.from(currentPos).setStyles(locationStyle))

                    markers.distinct().forEach { (lat, lng) ->
                        Log.d("MissingMapScreen", "마커 좌표: $lat, $lng")
                        layer.addLabel(
                            LabelOptions.from(LatLng.from(lat, lng))
                                .setStyles(locationStyle)
                        )
                    }
                }
            } else {
                // 위치 로딩 중
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/** Android Geocoder 기반 역지오코딩 (API 33 분기 처리) */
suspend fun geocodeAddress(context: Context, lat: Double, lng: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.KOREA)
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { list ->
                        val line = list?.firstOrNull()?.getAddressLine(0)
                        if (cont.isActive) cont.resume(line)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0)
            }
        } catch (e: Exception) {
            Log.e("MissingMapScreen", "Geocoder error", e)
            null
        }
    }
