package com.example.petplace.presentation.feature.walk_and_care

import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.Walk.WalkWriteForm
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkAndCareWriteScreen(
    navController: NavController,
    viewModel: WalkAndCareWriteViewModel = hiltViewModel(),
    onSubmit: (WalkWriteForm) -> Unit = {}
) {
    val context = LocalContext.current

// 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.updateRegionByLocation(context)
        }
    }

// 진입 시 권한 확인 후 regionId 업데이트
    LaunchedEffect(Unit) {
        val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val pm = ContextCompat.checkSelfPermission(context, fine)
        val pm2 = ContextCompat.checkSelfPermission(context, coarse)
        if (pm != PackageManager.PERMISSION_GRANTED &&
            pm2 != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(arrayOf(fine, coarse))
        } else {
            viewModel.updateRegionByLocation(context)
        }
    }
    // === colors ===
    val outline = Color(0xFFE5E7EB)
    val hint    = Color(0xFF9CA3AF)
    val accent  = Color(0xFFF79800)

    // === state ===
    val category     by viewModel.pickedCat.collectAsState()
    val title        by viewModel.title.collectAsState()
    val details      by viewModel.details.collectAsState()
    val startDate    by viewModel.startDate.collectAsState()
    val endDate      by viewModel.endDate.collectAsState()
    val startTime    by viewModel.startTime.collectAsState()
    val endTime      by viewModel.endTime.collectAsState()
    val imageUris    by viewModel.imageUris.collectAsState()
    val enableSubmit by viewModel.isValid.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val petName  by viewModel.petName.collectAsState()
    val petBreed by viewModel.petBreed.collectAsState()
    val petSex   by viewModel.petSex.collectAsState()
    val petBirthday by viewModel.petBirthday.collectAsState()
    val petImgSrc by viewModel.petImgSrc.collectAsState()

    // ✅ FamilySelectScreen에서 되돌아올 때 전달된 값 수신
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val selectedPetId by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<Long?>("pet_id", null)
    }?.collectAsState() ?: remember { mutableStateOf<Long?>(null) }

    val selectedName by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("pet_name", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    val selectedBreed by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("pet_breed", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    val selectedSex by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("pet_sex", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    val selectedBirthday by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("pet_birthday", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    val selectedImg by remember(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("pet_img", null)
    }?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    // ✅ 값이 들어오면 뷰모델로 반영하고, 키 제거(중복 반영 방지)
    LaunchedEffect(selectedPetId, selectedName, selectedBreed, selectedSex, selectedBirthday, selectedImg) {
        if (
            selectedPetId != null || selectedName != null || selectedBreed != null ||
            selectedSex != null || selectedBirthday != null || selectedImg != null
        ) {
            viewModel.setSelectedPet(
                name     = selectedName,
                breed    = selectedBreed,
                sex      = selectedSex,
                birthday = selectedBirthday,
                imgSrc   = selectedImg,
                id       = selectedPetId
            )
            savedStateHandle?.apply {
                remove<Long>("pet_id")
                remove<String>("pet_name")
                remove<String>("pet_breed")
                remove<String>("pet_sex")
                remove<String>("pet_birthday")
                remove<String>("pet_img")
            }
        }
    }

    val mode = remember(category) {
        when (category) {
            "산책구인", "산책의뢰" -> Mode.WALK
            "돌봄구인", "돌봄의뢰" -> Mode.CARE
            else -> Mode.NONE
        }
    }

    // === pickers ===
    var showDateRange  by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    // === gallery ===
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("돌봄/산책 구인 등록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onSubmit(
                        WalkWriteForm(
                            category  = category,
                            title     = title,
                            details   = details,
                            date      = when (mode) {
                                Mode.CARE -> {
                                    val s = startDate?.let { "%02d.%02d".format(it.monthValue, it.dayOfMonth) } ?: ""
                                    val e = endDate?.let   { "%02d.%02d".format(it.monthValue, it.dayOfMonth) } ?: ""
                                    if (s.isNotBlank() && e.isNotBlank()) "$s ~ $e" else ""
                                }
                                Mode.WALK -> ""
                                else -> ""
                            },
                            startTime = startTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            endTime   = endTime?.let   { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            image     = imageUris.firstOrNull()?.toString()
                        )
                    )
                    viewModel.submit(
                        onSuccess = { newId ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.apply {
                                    set("walk_post_created", true)     // ✅ 성공 신호
                                    set("walk_post_id", newId)         // 선택: 새 글 ID
                                }
                            navController.popBackStack()
                        },
                        onError = { /* TODO: snackbar */ }
                    )
                },
                enabled = enableSubmit && !isSubmitting,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(if (isSubmitting) "등록 중..." else "등록하기",
                    color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { inner ->
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(bottom = 96.dp)
        ) {
            // ================= 공통 영역 (항상 보임) =================

            // 1) 카테고리 선택 (칩)
            Spacer(Modifier.height(6.dp))
            Text("카테고리를 하나 선택해주세요.", fontSize = 13.sp, color = Color(0xFF6B7280))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("산책구인", "산책의뢰", "돌봄구인", "돌봄의뢰").forEach { cat ->
                    val selected = category == cat
                    Surface(
                        color = if (selected) accent else Color(0xFFFFFDF9),
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

            // 2) 제목
            Spacer(Modifier.height(16.dp))
            Text("제목", fontSize = 13.sp, fontWeight = FontWeight.Medium)
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

            /* ───── 반려동물 카드 ───── */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable { navController.navigate("family/select") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 이미지
                    val fullUrl = petImgSrc?.let {
                        if (it.startsWith("http")) it else "http://i13d104.p.ssafy.io:8081$it"
                    }
                    if (fullUrl != null) {
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.pp_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // 텍스트
                    Column(Modifier.weight(1f)) {
                        val nameText  = petName ?: "내 펫 선택"
                        val breedText = petBreed ?: "펫을 선택해주세요"

                        val sexKo = when (petSex?.uppercase()) {
                            "MALE"   -> "남아"
                            "FEMALE" -> "여아"
                            else     -> "성별미상"
                        }

                        val ageText = petBirthday?.let { b ->
                            runCatching {
                                val birth = java.time.LocalDate.parse(b)
                                val years = java.time.Period.between(birth, java.time.LocalDate.now()).years
                                "${years}살"
                            }.getOrElse { "" }
                        } ?: ""

                        Text(nameText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(breedText, fontSize = 12.sp, color = Color.Gray)
                        if (petName != null) {
                            Text("$sexKo $ageText", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            // 3) 이미지 업로드 + 미리보기
            Spacer(Modifier.height(16.dp))
            Text("펫 사진", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                ) { Icon(Icons.Default.Clear, contentDescription = "삭제", tint = Color.White) }
                            }
                        }
                    }
                }
            }

            // 4) 상세 내용
            Spacer(Modifier.height(16.dp))
            Text("상세 내용", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = details,
                onValueChange = viewModel::updateDetails,
                placeholder = {
                    Text("펫의 이름/특이사항/요청사항 등을 입력해주세요.", fontSize = 13.sp, color = hint)
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

            // ================= 모드별 영역 (이것만 스위칭) =================
            when (mode) {
                Mode.WALK -> {
                    Spacer(Modifier.height(16.dp))
                    Text("시간", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showStartPicker = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_access_time_24),
                                        tint = accent,
                                        contentDescription = "시작"
                                    )
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
                            value = endTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showEndPicker = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_access_time_24),
                                        tint = accent,
                                        contentDescription = "종료"
                                    )
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
                Mode.CARE -> {
                    Spacer(Modifier.height(16.dp))
                    Text("날짜(기간)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = buildString {
                            val s = startDate?.let { "%04d-%02d-%02d".format(it.year, it.monthValue, it.dayOfMonth) }
                            val e = endDate?.let   { "%04d-%02d-%02d".format(it.year, it.monthValue, it.dayOfMonth) }
                            append(listOfNotNull(s, e).joinToString(" ~ "))
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDateRange = true }) {
                                Icon(Icons.Default.DateRange, tint = accent, contentDescription = "기간")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = outline, unfocusedBorderColor = outline,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                        )
                    )
                }
                Mode.NONE -> Unit
            }
        }

        // ===== Picker 다이얼로그들 =====
        if (showStartPicker || showEndPicker) {
            val isStart = showStartPicker
            val init = (if (isStart) startTime else endTime) ?: LocalTime.now()
            val tp = rememberTimePickerState(init.hour, init.minute, is24Hour = false)
            AlertDialog(onDismissRequest = { showStartPicker = false; showEndPicker = false }) {
                Surface(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isStart) "시작 시간" else "종료 시간", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        TimeInput(state = tp)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showStartPicker = false; showEndPicker = false }) { Text("취소") }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                val t = LocalTime.of(tp.hour, tp.minute)
                                if (isStart) viewModel.setStartTime(t) else viewModel.setEndTime(t)
                                showStartPicker = false; showEndPicker = false
                            }) { Text("확인") }
                        }
                    }
                }
            }
        }

        if (showDateRange) {
            val zone = ZoneId.systemDefault()
            val s = startDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
            val e = endDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()

            // ✅ 오늘 포함 과거 불가
            val onlyFuture = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val candidate = Instant.ofEpochMilli(utcTimeMillis).atZone(zone).toLocalDate()
                    val today = java.time.LocalDate.now(zone)
                    // 오늘 포함 과거 불가 → 내일부터 선택 가능
                    return candidate.isAfter(today)
                }
                override fun isSelectableYear(year: Int): Boolean = true
            }

            val rp = rememberDateRangePickerState(
                initialSelectedStartDateMillis = s,
                initialSelectedEndDateMillis = e,
                selectableDates = onlyFuture
            )

            DatePickerDialog(
                onDismissRequest = { showDateRange = false },
                confirmButton = {
                    TextButton(onClick = {
                        rp.selectedStartDateMillis?.let {
                            viewModel.setStartDate(Instant.ofEpochMilli(it).atZone(zone).toLocalDate())
                        }
                        rp.selectedEndDateMillis?.let {
                            viewModel.setEndDate(Instant.ofEpochMilli(it).atZone(zone).toLocalDate())
                        }
                        showDateRange = false
                    }) { Text("확인") }
                },
                dismissButton = { TextButton(onClick = { showDateRange = false }) { Text("취소") } }
            ) {
                DateRangePicker(
                    state = rp,
                    title = {
                        Text(
                            text = "날짜 선택",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            textAlign = TextAlign.Center
                        )
                    },
                    headline = null,
                    showModeToggle = true // 연필 아이콘 유지
                )
            }
        }

    }
}

private enum class Mode { WALK, CARE, NONE }
