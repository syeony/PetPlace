@file:Suppress("NewApi") // Desugaring 사용 시 Lint 경고 억제(선택)

package com.example.petplace.presentation.feature.missing_report

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.feature.missing_register.RegisterViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// 색상 상수 추가 (파일 위쪽)
private val BgColor = Color(0xFFFEF9F0)
private val AccentOrange = Color(0xFFF79800)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun ReportScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val ui by reportViewModel.uiState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    // 현재 위치 권한 & 자동 채움
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { loc ->
                    scope.launch {
                        val addr = geocodeAddress(context, loc.latitude, loc.longitude)
                        reportViewModel.setAutoAddress(
                            address = addr ?: "${loc.latitude}, ${loc.longitude}",
                            lat = loc.latitude,
                            lng = loc.longitude
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { locationPermissionState.launchPermissionRequest() }
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()
    // 갤러리 이미지
    val imageList by registerViewModel.imageList.collectAsState()
    LaunchedEffect(imageList) {
        if (imageList.isNotEmpty()) reportViewModel.analyzeImagesForPet(imageList)
    }
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (!uris.isNullOrEmpty()) registerViewModel.addImages(uris)
        }

    Scaffold(
        containerColor = BgColor,   // ✅ 배경색 지정
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("목격 제보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Button(
                enabled = !ui.loading,
                onClick = {
                    val regionId = userInfo?.regionId
                    reportViewModel.submitSightingFromUris(
                        imageUris = imageList,
                        onSuccess = {
                            navController.navigate("nav_feed") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onFailure = {
                            //실패시
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = if (ui.loading) "전송 중..." else "작성 완료", color = Color.White, fontSize = 16.sp)
            }
        }
    ) { innerPadding ->
        // ✅ 전체 스크롤 가능 영역: LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp // bottom bar 높이만큼 여유
            ),
            verticalArrangement = Arrangement.Top
        ) {
            // 이미지 선택 + 썸네일
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable {
                                launcherGallery.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_photo_camera_24),
                                contentDescription = "Upload Image",
                                modifier = Modifier.size(36.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${imageList.size} / 5", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(imageList) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFD7D7D7)), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 검출 결과/로딩/실패
            item {
                Spacer(Modifier.height(12.dp))
                when {
                    !ui.detectionChecked -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(ui.detectionMessage, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    ui.annotatedBitmap != null -> {
                        val bmp = ui.annotatedBitmap!!
                        val ratio = remember(bmp) { (bmp.width.toFloat() / bmp.height.coerceAtLeast(1).toFloat()) }
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Detection result",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp))
                                .aspectRatio(ratio),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = ui.detectionMessage,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))

                        if (ui.dogResults.isNotEmpty()) {
                            Text(
                                text = "감지된 품종",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                            LazyRow(modifier = Modifier.fillMaxWidth()) {
                                items(ui.dogResults) { r ->
                                    BreedChip("${r.breedLabel} ${(r.breedProb * 100).roundToInt()}%")
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = ui.detectionMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 설명
            item {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = ui.description,
                    onValueChange = reportViewModel::updateDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            "목격 장소, 상황, 특징 등을 작성해주세요. 애타게 찾고 있는 집사님께 큰 도움이 됩니다.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // 목격 일시
            item {
                Spacer(Modifier.height(16.dp))
                Text("목격 일시", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 날짜
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = ui.selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    // 시간
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { showTimePicker = true },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = ui.selectedTime.format(DateTimeFormatter.ofPattern("a HH:mm", Locale.KOREAN)),
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // 목격 장소
            item {
                Spacer(Modifier.height(16.dp))
                Text("목격 장소", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { navController.navigate("missing_map") },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = ui.selectedAddress,
                        modifier = Modifier.padding(start = 12.dp),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "작성 완료 후에는 장소를 변경할 수 없어요",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 날짜 선택
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = ui.selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        reportViewModel.setDate(
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }

    // 시간 선택
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = ui.selectedTime.hour,
            initialMinute = ui.selectedTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            modifier = Modifier.wrapContentSize(),
        ) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "시간 선택", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    TimeInput(state = timePickerState)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("취소") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            reportViewModel.setTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        }) { Text("확인") }
                    }
                }
            }
        }
    }

    // ==== 지도에서 돌아온 값 수신 ====
    val handle = navController.currentBackStackEntry?.savedStateHandle
    val addressFlow = remember(handle) { handle?.getStateFlow("selected_address", null as String?) ?: MutableStateFlow(null) }
    val latFlow     = remember(handle) { handle?.getStateFlow("selected_lat", null as Double?) ?: MutableStateFlow(null) }
    val lngFlow     = remember(handle) { handle?.getStateFlow("selected_lng", null as Double?) ?: MutableStateFlow(null) }

    val selectedAddress by addressFlow.collectAsState()
    val selLat by latFlow.collectAsState()
    val selLng by lngFlow.collectAsState()

    LaunchedEffect(selectedAddress, selLat, selLng) {
        if (selectedAddress != null) {
            reportViewModel.setManualAddress(
                address = selectedAddress!!,
                lat = selLat,
                lng = selLng
            )
            handle?.remove<String>("selected_address")
            handle?.remove<Double>("selected_lat")
            handle?.remove<Double>("selected_lng")
        }
    }
}

@Composable
private fun BreedChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color(0xFF333333))
    }
}
