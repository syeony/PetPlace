package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.example.petplace.R
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodViewModel
import com.example.petplace.presentation.feature.chat.ChatListScreen
import com.example.petplace.util.CommonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingMapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: NeighborhoodViewModel = hiltViewModel()

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
        mutableStateOf(
            LabelStyles.from(LabelStyle.from(R.drawable.location_on))
        )
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
//                    text = addressText,
                    text = "sss", // 마커가 위치한 주소로 변경
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
//                            selectedLatLng?.let {
//                                navController.previousBackStackEntry?.savedStateHandle
//                                    ?.set("selected_location", currentAddress)
//                                navController.popBackStack()
//                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "이 위치로 설정", fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (currentLat != null && currentLng != null) {
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
                                        map.labelManager?.layer?.addLabel(
                                            LabelOptions.from(pos).setStyles(locationStyle)
                                        )
                                        map.moveCamera(
                                            CameraUpdateFactory.newCenterPosition(
                                                pos,
                                                15
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

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


                LaunchedEffect(markers, currentLat, currentLng) {
                    val map = kakaoMapState.value ?: return@LaunchedEffect
                    val layer = map.labelManager?.layer ?: return@LaunchedEffect

                    val currentPos = LatLng.from(
                        currentLat ?: return@LaunchedEffect,
                        currentLng ?: return@LaunchedEffect
                    )
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
