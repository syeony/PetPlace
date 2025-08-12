package com.example.petplace.presentation.feature.hotel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
import kotlin.math.log

@SuppressLint("StateFlowValueCalledInComposition", "UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel
) {

    val currentMonth = YearMonth.now()
    val startMonth = currentMonth
    val endMonth = currentMonth.plusMonths(6)
    val today = LocalDate.now()

    val reservationState by viewModel.reservationState.collectAsState()
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plusDays(1)) }
    Log.d("animal" , "고른 동물 :${reservationState.selectedAnimal}")
    // 날짜 변경될 때마다 ViewModel에 반영
    LaunchedEffect(startDate, endDate) {
        viewModel.selectDate(
            startDate?.toString().orEmpty(),
            endDate?.toString().orEmpty()
        )
    }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center // 세로 중앙 정렬
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
                modifier = Modifier.height(48.dp), // 높이 줄이기
                windowInsets = WindowInsets(0.dp)  // 상단 패딩 제거
            )
        }
    ) {
        Column(
            modifier = Modifier
//                .padding(innerPadding)// 이거 쓰면 bottomnav만큼 여백생김
                .padding(0.dp)
                .fillMaxSize()
            ,
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
                            val prevMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
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
                            val nextMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
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
                                    shape = if (isStart || isEnd || isTodayHighlight) RoundedCornerShape(
                                        0.dp
                                    ) else RoundedCornerShape(0.dp)
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

            // 선택된 날짜 표시 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "선택된 날짜",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray,
                    style = AppTypography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                        .background(Color.LightGray)
                        .padding(24.dp),

                    ) {

                    Text(
                        text = displayText,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = Color.Black,
                        style = AppTypography.bodyLarge
                    )
                }
            }

            //마리수
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "댕/냥원(마릿수)",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray,
                    style = AppTypography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                        .padding(8.dp)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 감소 버튼
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        onClick = {
                            viewModel.decreaseAnimalCount()
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "감소")
                    }

                    // 현재 마릿수
                    Text(
                        text = "${reservationState.animalCount} 마리",
                        style = AppTypography.labelLarge
                    )
                    // 증가 버튼
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        onClick = {
                            viewModel.increaseAnimalCount()
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "증가")
                    }
                }


            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (startDate == null) {
                        startDate = today
                    }
                    if (endDate == null) {
                        endDate = startDate
                    }
                    viewModel.selectDate(
                        startDate?.toString().orEmpty(),
                        endDate?.toString().orEmpty()
                    )
                    Log.d("당시","${startDate}  ${endDate}")
                    navController.navigate("hotel/list")
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
