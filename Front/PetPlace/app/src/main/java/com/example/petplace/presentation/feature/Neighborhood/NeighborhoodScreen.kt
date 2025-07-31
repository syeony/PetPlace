package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.util.CommonUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen(
    navController: NavController,
    initialShowDialog: Boolean = false,
    viewModel: NeighborhoodViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    // 뷰모델에서 가져오는 상태들
    val tags        = viewModel.tags
    val selectedTag by viewModel.selectedTag.collectAsState()
    val showSheet   by viewModel.showBottomSheet.collectAsState()

    // 1) 위/경도 상태
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }

    // 2) 화면 진입 시 위/경도 조회
    LaunchedEffect(Unit) {
        CommonUtils.getXY(context)?.let { (lat, lon) ->
            currentLat = lat
            currentLng = lon
        }
    }

    // 바텀시트 상태 (부분 확장 허용)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // 1) 맵 또는 로딩
        if (currentLat != null && currentLng != null) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {}
                                override fun onMapError(e: Exception?) {}
                                override fun onMapResumed() {}
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    val pos = LatLng.from(currentLat!!, currentLng!!)
                                    map.labelManager?.layer?.addLabel(
                                        LabelOptions.from(pos)
                                            .setStyles(R.drawable.location_on)
                                    )
                                    map.moveCamera(
                                        CameraUpdateFactory.newCenterPosition(pos, 15)
                                    )
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            // 아직 위/경도 없으면 로딩
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // 2) 검색창 + 태그 Row (맵 위에 오버레이)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("애견 동반 장소를 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Row(Modifier.horizontalScroll(rememberScrollState())) {
                tags.forEach { tag ->
                    val isSel = selectedTag == tag
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color(0xFFF79800) else Color(0xFFF5F5F5))
                            .clickable { viewModel.selectTag(tag) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(tag, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }
        }

        // 3) showSheet 가 true 이면, 분리된 파일의 BottomSheet 호출
        if (showSheet) {
            NeighborhoodBottomSheet(
                onDismiss = { scope.launch { sheetState.hide() } },
                sheetState = sheetState,
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)               // ← 이 줄이 핵심!
                    .navigationBarsPadding()
            )
        }
    }
}