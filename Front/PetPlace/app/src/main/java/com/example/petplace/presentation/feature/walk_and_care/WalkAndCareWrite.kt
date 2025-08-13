package com.example.petplace.presentation.feature.walk_and_care

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.data.local.Walk.WalkWriteForm
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

private val AccentOrange = Color(0xFFF79800)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkAndCareWriteScreen(
    navController: NavController,
    viewModel: WalkAndCareWriteViewModel = hiltViewModel(),
    // 필요하면 외부 콜백도 유지
    onSubmit: (WalkWriteForm) -> Unit = {}
) {
    val orange  = Color(0xFFF79800)
    val outline = Color(0xFFE5E7EB)
    val hint    = Color(0xFF9CA3AF)

    // state collect
    val pickedCat     by viewModel.pickedCat.collectAsState()
    val title         by viewModel.title.collectAsState()
    val details       by viewModel.details.collectAsState()
    val date          by viewModel.date.collectAsState()
    val startTime     by viewModel.startTime.collectAsState()
    val endTime       by viewModel.endTime.collectAsState()
    val imageUris     by viewModel.imageUris.collectAsState()
    val enableSubmit  by viewModel.isValid.collectAsState()
    val isSubmitting  by viewModel.isSubmitting.collectAsState()
    val eventFlow     = viewModel.event

    // snackbars
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        eventFlow.collect { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
        }
    }

    // pickers show/hide
    var showDatePicker  by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    // 갤러리 (최대 5장)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                }
                Text(
                    text = "돌봄/산책 구인 등록",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    // 기존 외부 콜백 (원하면 삭제 가능)
                    onSubmit(
                        WalkWriteForm(
                            category  = pickedCat,
                            title     = title,
                            details   = details,
                            date      = date?.let { "%02d.%02d".format(it.monthValue, it.dayOfMonth) } ?: "",
                            startTime = startTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            endTime   = endTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            image     = imageUris.firstOrNull()?.toString()
                        )
                    )
                    // 실제 서버 등록
                    viewModel.submit(
                        onSuccess = { /* 성공 시 원하는 곳으로 이동 */ navController.popBackStack() },
                        onError = { /* 스낵바에서 이미 표시됨 */ }
                    )
                },
                enabled = enableSubmit && !isSubmitting,             // ✅ 활성화 조건
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            ) {
                Text(if (isSubmitting) "등록 중..." else "등록하기", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { inner ->
        val scroll = rememberScrollState()

        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 스크롤 본문
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 96.dp)     // 하단 버튼 영역 고려
            ) {
                Spacer(Modifier.height(6.dp))
                Text("카테고리를 하나 선택해주세요.", fontSize = 13.sp, color = Color(0xFF6B7280))
                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰").forEach { cat ->
                        val selected = pickedCat == cat
                        Surface(
                            color = if (selected) orange else Color(0xFFFFFDF9),
                            contentColor = if (selected) Color.White else Color(0xFF374151),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clickable { viewModel.selectCategory(cat) }
                                .border(
                                    width = if (selected) 0.dp else 1.dp,
                                    color = if (selected) Color.Transparent else Color(0xFFFFE0B3),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(cat, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("제목", fontSize = 13.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = { Text("제목을 입력해주세요", fontSize = 13.sp, color = hint) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = outline, unfocusedBorderColor = outline,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("펫 사진", fontSize = 13.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))

                // 업로드 카드 + 미리보기
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 업로드 카드
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .border(1.dp, Color(0xFFD7D7D7), RoundedCornerShape(12.dp))
                            .clickable {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text("${imageUris.size} / 5", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    // 프리뷰들
                    if (imageUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(imageUris) { uri ->
                                Box {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFD7D7D7), RoundedCornerShape(12.dp))
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeImage(uri) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-6).dp)
                                            .size(24.dp)
                                            .background(Color(0x66000000), RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "삭제", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("상세 내용", fontSize = 13.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = viewModel::updateDetails,
                    placeholder = {
                        Text(
                            "펫의 이름과 특이사항(질병, 약 투여 등), 하고싶은 말 등을 입력해주세요.",
                            fontSize = 13.sp,
                            color = hint
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = outline, unfocusedBorderColor = outline,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("날짜", fontSize = 13.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = viewModel.dateText(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "날짜")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = outline, unfocusedBorderColor = outline,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("시간", fontSize = 13.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.startText(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStartPicker = true }) {
                                Icon(Icons.Default.Info, contentDescription = "시작")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outline, unfocusedBorderColor = outline,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = viewModel.endText(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndPicker = true }) {
                                Icon(Icons.Default.Info, contentDescription = "종료")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outline, unfocusedBorderColor = outline,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                        )
                    )
                }
            }
        }
    }

    // DatePicker
    if (showDatePicker) {
        val init = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: Instant.now().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = init)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        viewModel.setDate(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = state)
        }
    }

    // TimePicker
    if (showStartPicker || showEndPicker) {
        val isStart = showStartPicker
        val init = (if (isStart) startTime else endTime) ?: LocalTime.now()
        val state = rememberTimePickerState(
            initialHour = init.hour,
            initialMinute = init.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = {
                showStartPicker = false
                showEndPicker = false
            }
        ) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (isStart) "시작 시간" else "종료 시간", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    TimeInput(state = state)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            showStartPicker = false
                            showEndPicker = false
                        }) { Text("취소") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val t = LocalTime.of(state.hour, state.minute)
                            if (isStart) viewModel.setStartTime(t) else viewModel.setEndTime(t)
                            showStartPicker = false
                            showEndPicker = false
                        }) { Text("확인") }
                    }
                }
            }
        }
    }
}
