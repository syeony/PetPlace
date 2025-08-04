package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.petplace.util.CommonUtils

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

    Box(modifier = Modifier.fillMaxSize()) {
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

            LaunchedEffect(markers, currentLat, currentLng) {
                val map = kakaoMapState.value ?: return@LaunchedEffect
                val layer = map.labelManager?.layer ?: return@LaunchedEffect

                val currentPos = LatLng.from(currentLat ?: return@LaunchedEffect, currentLng ?: return@LaunchedEffect)
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
