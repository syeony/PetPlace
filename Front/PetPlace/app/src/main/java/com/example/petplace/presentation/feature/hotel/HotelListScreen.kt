package com.example.petplace.presentation.feature.hotel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val reservationState by viewModel.reservationState.collectAsState()

    // 🔹 화면에서 선택한 날짜 상태
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // 🔹 ViewModel 상태와 동기화
    LaunchedEffect(reservationState.checkInDate, reservationState.checkOutDate) {
        startDate = reservationState.checkInDate?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
        endDate = reservationState.checkOutDate?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
    }

    Log.d("check", "checkIn=${reservationState.checkInDate}, checkOut=${reservationState.checkOutDate}")

    val currentMonth = YearMonth.now()
    val calendarState = rememberCalendarState(
        startMonth = currentMonth,
        endMonth = currentMonth.plusMonths(6),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { expanded = !expanded }
                    ) {
                        val displayText = when {
                            startDate != null && endDate != null -> "${startDate} ~ ${endDate}"
                            startDate != null -> startDate.toString()
                            else -> "체크인 / 체크아웃"
                        }
                        Text(text = displayText, style = AppTypography.bodyLarge)
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // 🔹 달력 & 마릿수 선택 박스
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 월 이동 헤더
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val prevMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                                coroutineScope.launch { calendarState.animateScrollToMonth(prevMonth) }
                            }
                        ) { Text("<") }

                        Text(
                            text = "${calendarState.firstVisibleMonth.yearMonth.year}년 ${calendarState.firstVisibleMonth.yearMonth.monthValue}월",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = {
                                val nextMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                                coroutineScope.launch { calendarState.animateScrollToMonth(nextMonth) }
                            }
                        ) { Text(">") }
                    }

                    // 🔹 달력
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            val isStart = day.date == startDate
                            val isEnd = day.date == endDate
                            val inRange = startDate != null && endDate != null &&
                                    day.date.isAfter(startDate) && day.date.isBefore(endDate)

                            val bgColor = when {
                                isStart || isEnd -> Color(0xFFFFA000)
                                inRange -> Color(0xFFFFECB3)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(bgColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = day.position == DayPosition.MonthDate) {
                                        if (startDate == null || (startDate != null && endDate != null)) {
                                            startDate = day.date
                                            endDate = null
                                        } else if (startDate != null && endDate == null) {
                                            endDate = if (day.date.isBefore(startDate)) {
                                                startDate.also { startDate = day.date }
                                            } else day.date
                                        }

                                        // 🔹 선택될 때마다 ViewModel 업데이트
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 마릿수 선택
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.decreaseAnimalCount() }) {
                            Text("-")
                        }
                        Text("${reservationState.animalCount} 마리")
                        IconButton(onClick = { viewModel.increaseAnimalCount() }) {
                            Text("+")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { expanded = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("적용하기", color = Color.White)
                    }
                }
            }

            // 🔹 호텔 리스트
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "예약 가능한 호텔 리스트",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = AppTypography.bodyLarge
            )
        }
    }
}
