package com.example.petplace.presentation.feature.Neighborhood

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen(
    navController: NavController,
    initialShowDialog: Boolean = false,
//    viewModel: NeighborhoodViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val viewModel: NeighborhoodViewModel = hiltViewModel()
    val showAdoptConfirm by viewModel.showAdoptConfirm.collectAsState()

    // 입양처 다이얼로그 확인창
    if (showAdoptConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAdoptConfirm(false) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.notification),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(60.dp)
                )
            },
            title = {
                Text(
                    "알림",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "유기보호동물 보호소 홈페이지로 이동합니다.\n 가족이 되어주세요.",
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                    color      = Color.Gray,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "확인",
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val url = "https://www.animal.go.kr/front/awtis/public/publicList.do?menuNo=1000000055"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                viewModel.setShowAdoptConfirm(false)
                            },
                        color = Color(0xFFF79800),
                        textAlign = TextAlign.Center
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "취소",
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { viewModel.setShowAdoptConfirm(false) },
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = Color.White
        )
    }


    /* -------- ViewModel state -------- */
    val tags = viewModel.tags                 // List<TagItem>
    val selectedTag by viewModel.selectedTag.collectAsState()
    val showSheet by viewModel.showBottomSheet.collectAsState()
    val showThanks by viewModel.showThanksDialog.collectAsState()


    // 위치 조회
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(Unit) {
        CommonUtils.getXY(context)?.let { (lat, lon) ->
            currentLat = lat
            currentLng = lon
        }
    }
    // Composable 내부
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val kakaoMap = remember { mutableStateOf<KakaoMap?>(null) }
    // BottomSheetScaffold state
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    // showSheet 플래그에 따라 expand/collapse
    LaunchedEffect(showSheet) {
        if (showSheet) scope.launch { scaffoldState.bottomSheetState.expand() }
        else scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }

    // 최초 진입 시 Thanks Dialog
    LaunchedEffect(Unit) {
        if (initialShowDialog) viewModel.setThanksDialog(true)
    }

    // Thanks Dialog
    if (showThanks) {
        MatchingThanksDialog { viewModel.setThanksDialog(false) }
    }
    val markers by viewModel.markers.collectAsState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 32.dp,  // collapsed 상태에서 handle 높이
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContainerColor = Color.White,
        sheetContent = {

            // 시트 내부 버튼들
            val buttons = listOf(
                Triple("실종펫 등록", R.drawable.caution, Color(0xFFFFC9C5)),
                Triple("실종펫 신고", R.drawable.search, Color(0xFFD0E4FF)),
                Triple("실종펫 리스트", R.drawable.checklist, Color(0xFFFFE4C1)),
                Triple("돌봄/산책", R.drawable.walk, Color(0xFFCBF4D1)),
                Triple("입양처", R.drawable.feelings, Color(0xFFFAD3E4)),
                Triple("동물호텔", R.drawable.hotel, Color(0xFFE6D5FF))
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(2f / 4f)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // ✅ 일정 간격
                    ) {
                        row.forEach { (label, iconRes) ->
                            FeatureButton(
                                label = label,
                                icon = iconRes,         // ← 실제 아이콘 전달
                                modifier = Modifier.weight(1f) // ✅ 동일 비율로 확장/축소
                            ) {
                                // 클릭 핸들
                                when (label) {
                                    "실종펫 등록" -> navController.navigate("missing_register")
                                    "실종펫 신고" -> navController.navigate("missing_report")
                                    "돌봄/산책"    -> navController.navigate("walk_and_care")
                                    "입양처" -> {
                                        viewModel.setShowAdoptConfirm(true)
                                    }
                                    "실종펫 리스트" -> navController.navigate("missing_list")
//                                    "실종펫 리스트" ->navController.navigate("missingReportDetail/23")

                                    "동물호텔" -> navController.navigate("hotel_graph")
                                }
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
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
                Log.d("logg", "$currentLat  $currentLng")
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView.value = this
                            start(
                                object : MapLifeCycleCallback() {
                                    override fun onMapDestroy() {}
                                    override fun onMapError(e: Exception?) {}
                                    override fun onMapResumed() {}
                                },
                                object : KakaoMapReadyCallback() {
                                    override fun onMapReady(map: KakaoMap) {
                                        kakaoMap.value = map

                                        val pos = LatLng.from(currentLat!!, currentLng!!)
                                        // 현재 위치 마커
                                        map.labelManager?.layer?.addLabel(
                                            LabelOptions.from(pos)
                                                .setStyles(R.drawable.location_on)
//                                                .setStyles(style)  // style 객체 사용

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
                val style by remember {
                    mutableStateOf(
                        LabelStyles.from(LabelStyle.from(R.drawable.marker_resized_48x48))
                    )
                }
                LaunchedEffect(markers) {
                    val map = kakaoMap.value ?: return@LaunchedEffect
                    val layer = map.labelManager?.layer ?: return@LaunchedEffect

                    // 현재 위치 마커만 유지하고 태그 마커만 갱신
                    val currentPos = LatLng.from(currentLat ?: return@LaunchedEffect, currentLng ?: return@LaunchedEffect)
                    layer.removeAll()
                    layer.addLabel(LabelOptions.from(currentPos).setStyles(style))

                    markers.distinct().forEach { (lat, lng) ->
                        Log.d("Neighborhood", "마커 좌표: $lat, $lng")
                        layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
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
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFF79800)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(color = Color.White)
                        .border(1.dp, Color(0xFFFFC981),
                            shape = RoundedCornerShape(30.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                /* 태그 리스트 */
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    tags.forEach { tagItem ->
                        val isSelected = selectedTag == tagItem
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    color = if (isSelected) Color(0xFFF79800) else Color.White
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFFFC981),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.selectTag(tagItem)
                                    currentLng?.let {
                                        currentLat?.let { it1 ->
                                            viewModel.searchPlaces(tagItem.label,
                                                currentLat!!, currentLng!!
                                            )
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(tagItem.iconRes),
                                    contentDescription = tagItem.label,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.Unspecified     // PNG 고유색 유지
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    tagItem.label,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}