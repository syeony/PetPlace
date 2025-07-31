package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem.Chat.icon
import com.example.petplace.util.CommonUtils
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberBottomSheetScaffoldState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen(
    navController: NavController,
    initialShowDialog: Boolean = false,
    viewModel: NeighborhoodViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    // ViewModel state
    val tags        = viewModel.tags
    val selectedTag by viewModel.selectedTag.collectAsState()
    val showSheet   by viewModel.showBottomSheet.collectAsState()
    val showThanks  by viewModel.showThanksDialog.collectAsState()

    // 위치 조회
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(Unit) {
        CommonUtils.getXY(context)?.let { (lat, lon) ->
            currentLat = lat
            currentLng = lon
        }
    }

    // BottomSheetScaffold state
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    // showSheet 플래그에 따라 expand/collapse
    LaunchedEffect(showSheet) {
        if (showSheet) scope.launch { scaffoldState.bottomSheetState.expand() }
        else           scope.launch { scaffoldState.bottomSheetState.hide() }
    }

    // 최초 진입 시 Thanks Dialog
    LaunchedEffect(Unit) {
        if (initialShowDialog) viewModel.setThanksDialog(true)
    }

    // Thanks Dialog
    if (showThanks) {
        MatchingThanksDialog { viewModel.setThanksDialog(false) }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 32.dp,  // collapsed 상태에서 handle 높이
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            // Drag handle
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }

            // 시트 내부 버튼들
            val buttons = listOf(
                Triple("실종펫 등록",  R.drawable.outline_exclamation_24, Color(0xFFFFC9C5)),
                Triple("실종펫 신고",  R.drawable.outline_search_24,      Color(0xFFD0E4FF)),
                Triple("실종펫 리스트", R.drawable.ic_feed,                Color(0xFFFFE4C1)),
                Triple("돌봄/산책",    R.drawable.outline_sound_detection_dog_barking_24, Color(0xFFCBF4D1)),
                Triple("입양처",       R.drawable.outline_home_work_24,   Color(0xFFFAD3E4)),
                Triple("동물호텔",     R.drawable.outline_home_work_24,   Color(0xFFE6D5FF))
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(2f / 3f)
            ) {
                Text(
                    "우리동네 한눈에 보기",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                buttons.chunked(3).forEach { row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { (label, _, bgColor) ->
                            FeatureButton(
                                label, icon, bgColor
                            ) {
                                // 4) 클릭 핸들
                                when (label) {
                                    "실종펫 등록" -> navController.navigate("Missing_register")
                                    "실종펫 신고" -> navController.navigate("missing_report")
                                }
                                scope.launch {
                                    scaffoldState.bottomSheetState.hide()
                                    viewModel.hideBottomSheet()
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1) Map or Loading
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
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 2) 검색창 + 태그 오버레이
            Column(
                Modifier
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
                        Box(
                            Modifier
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
        }
    }
}
