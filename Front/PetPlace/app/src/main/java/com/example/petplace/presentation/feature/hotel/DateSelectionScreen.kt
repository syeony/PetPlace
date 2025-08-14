package com.example.petplace.presentation.feature.hotel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundColor
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@SuppressLint("StateFlowValueCalledInComposition", "UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel = hiltViewModel()
) {
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth
    val endMonth = currentMonth.plusMonths(6)
    val today = LocalDate.now()

    val reservationState by viewModel.reservationState.collectAsState()

    // null 허용: 날짜 범위 선택 UX를 위해
    var startDate by remember { mutableStateOf<LocalDate?>(today) }
    var endDate by remember { mutableStateOf<LocalDate?>(today.plusDays(1)) }

    Log.d("animal", "고른 동물 :${reservationState.selectedAnimal}")

    // 날짜 변경될 때마다 ViewModel에 반영
    LaunchedEffect(startDate, endDate) {
        viewModel.selectDate(
            startDate?.toString().orEmpty(),
            endDate?.toString().orEmpty()
        )
    }

    // 화면 진입 시 한 번 실행
    LaunchedEffect(Unit) {
        viewModel.getMyPets()
    }

    val petList by viewModel.myPetList.collectAsState()

    var selectedPetId by remember { mutableStateOf<Int?>(null) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "날짜 선택",
                            style = AppTypography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundColor
                ),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp) // 상단 패딩 제거
            )
        }
    ) {
        Column(
            modifier = Modifier
                // .padding(innerPadding)  // 이거 쓰면 bottomnav만큼 여백생김
                .padding(0.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 달력 박스
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                // 월 이동 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prevMonth =
                                calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                            coroutineScope.launch { calendarState.animateScrollToMonth(prevMonth) }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "이전 달")
                    }

                    Text(
                        text = "${calendarState.firstVisibleMonth.yearMonth.year}년 ${calendarState.firstVisibleMonth.yearMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = {
                            val nextMonth =
                                calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                            coroutineScope.launch { calendarState.animateScrollToMonth(nextMonth) }
                        }
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "다음 달")
                    }
                }

                // 달력 본체
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        val isStart = day.date == startDate
                        val isEnd = day.date == endDate
                        val inRange = startDate != null && endDate != null &&
                                day.date.isAfter(startDate) && day.date.isBefore(endDate)

                        val isTodayHighlight =
                            startDate == null && endDate == null && day.date == today

                        val bgColor = when {
                            isStart || isEnd || isTodayHighlight -> Color(0xFFFFA000)
                            inRange -> Color(0xFFFFECB3)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = bgColor,
                                    shape = if (isStart || isEnd || isTodayHighlight)
                                        RoundedCornerShape(0.dp)
                                    else RoundedCornerShape(0.dp)
                                )
                                .clickable(enabled = day.position == DayPosition.MonthDate) {
                                    if (startDate == null || (startDate != null && endDate != null)) {
                                        // 새로운 선택 시작
                                        startDate = day.date
                                        endDate = null
                                    } else if (startDate != null && endDate == null) {
                                        // 종료 날짜 선택
                                        endDate = if (day.date.isBefore(startDate)) {
                                            startDate.also { startDate = day.date }
                                        } else {
                                            day.date
                                        }
                                    }
                                    viewModel.selectDate(
                                        startDate?.toString().orEmpty(),
                                        endDate?.toString().orEmpty()
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Gray
                            )
                        }
                    }
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // 선택된 날짜 표시 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "선택된 날짜",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray,
                    style = AppTypography.labelLarge
                )
                Spacer(modifier = Modifier.height(20.dp))
                val displayText = when {
                    startDate != null && endDate != null -> "${startDate.toString()} ~ ${endDate.toString()}"
                    startDate != null -> startDate.toString()
                    else -> today.toString()
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(24.dp),
                ) {
                    Text(
                        text = displayText,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black,
                        style = AppTypography.bodyLarge
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // 내 펫 선택
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text(
                    text = "내 펫 선택",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray,
                    style = AppTypography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(petList) { pet ->
                        val isSelected = pet.id == selectedPetId

                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    selectedPetId = pet.id
                                    viewModel.selecMyPet(pet.id, pet.animal) // ViewModel에 선택 반영
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                2.dp,
                                if (isSelected) Color(0xFFFF9800) else Color(0xFFE0E0E0)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFF3E0) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 6.dp else 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                // === 이미지 붙이는 방식 (상대경로 → 절대경로 + Coil 옵션) ===
                                val request = ImageRequest.Builder(context)
                                    .data(pet.imgSrc.toFullImageUrl())
                                    // .addHeader("Authorization", "Bearer $token") // 보호 리소스면 사용
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_report_image)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .build()

                                AsyncImage(
                                    model = request,
                                    contentDescription = pet.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = pet.name,
                                    style = AppTypography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (startDate == null) startDate = today
                    if (endDate == null) endDate = startDate!!.plusDays(1)

                    viewModel.selectDate(
                        startDate?.toString().orEmpty(),
                        endDate?.toString().orEmpty()
                    )
                    Log.d("당시", "$startDate  $endDate")

                    if (viewModel.reservationState.value.selectedPetId != null) {
                        navController.navigate("hotel/list")
                    } else {
                        // TODO: 펫 선택 안내 (Snackbar/Toast)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("검색하기", color = Color.Black)
            }
        }
    }
}


private const val IMAGE_BASE_URL = "http://i13d104.p.ssafy.io:8081"

private fun String?.toFullImageUrl(): String? {
    val raw = this ?: return null
    return if (raw.startsWith("http", ignoreCase = true)) {
        raw
    } else {
        IMAGE_BASE_URL.trimEnd('/') + "/" + raw.trimStart('/')
    }
}
