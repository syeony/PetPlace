package com.example.petplace.presentation.feature.missing_report

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingMapScreen(navController: NavController) {
    var currentAddress by remember { mutableStateOf("지도를 움직여 위치를 설정하세요.") }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "지도에서 위치 확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    selectedLatLng?.let {
                        navController.previousBackStackEntry?.savedStateHandle?.set("selected_location", currentAddress)
                        navController.popBackStack()
                    }
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
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AndroidView(
                factory = { context ->
                    MapView(context)
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {}
                            override fun onMapError(error: Exception) {}
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(kakaoMap: KakaoMap) {
                                kakaoMap.setOnCameraMoveEndListener { _, _, _ ->
                                    val center = kakaoMap.cameraPosition?.position
                                    center?.let {
                                        selectedLatLng = it
                                        val geocoder = Geocoder(context, Locale.KOREAN)
                                        try {
                                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                            if (addresses?.isNotEmpty() == true) {
                                                currentAddress = addresses[0].getAddressLine(0)
                                            }
                                        } catch (e: Exception) {
                                            currentAddress = "주소를 찾을 수 없습니다."
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            )
            Text(
                text = currentAddress,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }
}
