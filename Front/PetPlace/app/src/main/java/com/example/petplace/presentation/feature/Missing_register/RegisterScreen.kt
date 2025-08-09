package com.example.petplace.presentation.feature.missing_register

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/* -------- 색상 상수 -------- */
private val BgColor      = Color(0xFFFEF9F0)
private val AccentOrange = Color(0xFFF79800)   // #F79800

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    /* ---------- 상태 ---------- */
    val detail   by viewModel.detail.collectAsState()
    val images   by viewModel.imageList.collectAsState()
    val date     by viewModel.date.collectAsState()
    val time     by viewModel.time.collectAsState()
    val place    by viewModel.place.collectAsState()

    /* ---------- 갤러리 런처 ---------- */
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (!uris.isNullOrEmpty()) viewModel.addImages(uris)
        }

    /* ---------- 로컬 UI 상태 ---------- */
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    /* ---------- Scaffold ---------- */
    Scaffold(
        containerColor = BgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실종 등록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
            )
        },

        /* ✔ 작성완료 버튼을 bottomBar 에 고정 */
        bottomBar = {
            Button(
                onClick = {
                    navController.navigate("${BottomNavItem.Neighborhood.route}?showDialog=true") {
                        popUpTo("Missing_register") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape  = RoundedCornerShape(8.dp)
            ) { Text("작성완료", color = Color.White, fontSize = 16.sp) }
        }
    ) { inner ->

        Column(
            modifier = Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

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
                    Image(
                        painter = painterResource(R.drawable.pp_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("코코", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("골든 리트리버 • 3살", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            /* ───── 사진 업로드 ───── */
            Row(
                Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /* + 버튼 */
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            BorderStroke(1.dp, Color(0xFFD7D7D7)),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            launcherGallery.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_photo_camera_24),
                        contentDescription = null,
                        tint = Color(0xFF8C8C8C)
                    )
                }

                /* 썸네일 */
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(images) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    BorderStroke(1.dp, Color(0xFFD7D7D7)),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C),
                lineHeight = 16.sp
            )

            /* ───── 상세 내용 ───── */
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = viewModel::setDetail,
                placeholder = {
                    Text("실종 장소, 상황, 특징 등을 작성해주세요.", color = Color(0xFFADAEBC))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            /* ───── 실종 일시 ───── */
            Spacer(Modifier.height(24.dp))
            Text("실종 일시", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                /* 날짜 박스 */
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true },
                    contentAlignment = Alignment.CenterStart
                ) { Text(date, Modifier.padding(start = 12.dp)) }

                /* 시간 박스 */
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .clickable { showTimePicker = true },
                    contentAlignment = Alignment.CenterStart
                ) { Text(time, Modifier.padding(start = 12.dp)) }
            }

            /* ───── 실종 장소 ───── */
            Spacer(Modifier.height(24.dp))
            Text("실종 장소", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = place,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("missing_map") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor   = Color(0xFFE0E0E0)
                )
            )

            Spacer(Modifier.height(4.dp))
            Text(
                "작성 완료 후에는 장소를 변경할 수 없어요.",
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C)
            )

            /* bottomBar 공간 확보 */
            Spacer(Modifier.height(80.dp))
        }
    }

    /* ---------- 날짜 피커 ---------- */
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        val str = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                        viewModel.setDate(str)
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton({ showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = pickerState) }
    }

    /* ---------- 시간 피커 ---------- */
    if (showTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimeInput(state = timeState)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("취소") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val str = LocalTime.of(timeState.hour, timeState.minute)
                                .format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
                            viewModel.setTime(str)
                            showTimePicker = false
                        }) { Text("확인") }
                    }
                }
            }
        }
    }

    /* ---------- 지도 결과 수신 ---------- */
    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_location")
        ?.let { selected ->
            viewModel.setPlace(selected)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_location")
        }
}
