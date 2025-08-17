package com.example.petplace.presentation.feature.hotel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private const val IMAGE_BASE_URL = "http://i13d104.p.ssafy.io:8081/"

private fun resolveImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val trimmed = raw.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        val base = IMAGE_BASE_URL.trimEnd('/')
        val path = trimmed.trimStart('/')
        "$base/$path"
    }
}

/* ───────────────────────────── */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel = hiltViewModel(),
) {
    var expanded by remember { mutableStateOf(false) }
    val reservationState by viewModel.reservationState.collectAsState()

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    val hotelList by viewModel.hotelList.collectAsState()

    LaunchedEffect(reservationState.checkInDate, reservationState.checkOutDate) {
        startDate = reservationState.checkInDate?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
        endDate = reservationState.checkOutDate?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.getHotelList()
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
                            startDate != null && endDate != null -> "$startDate ~ $endDate"
                            startDate != null -> startDate.toString()
                            else -> "체크인 / 체크아웃"
                        }
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = displayText, style = AppTypography.bodyLarge)
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedVisibility(visible = expanded) {
                DatePickerSheet(
                    onPrevMonth = {
                        val prevMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                        coroutineScope.launch { calendarState.animateScrollToMonth(prevMonth) }
                    },
                    onNextMonth = {
                        val nextMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                        coroutineScope.launch { calendarState.animateScrollToMonth(nextMonth) }
                    },
                    monthLabel = "${calendarState.firstVisibleMonth.yearMonth.year}년 ${calendarState.firstVisibleMonth.yearMonth.monthValue}월",
                    calendar = {
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
                    },
                    onApply = { expanded = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "예약 가능한 호텔 리스트",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                style = AppTypography.bodyLarge
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(hotelList) { item ->
                    RoomPriceCard(
                        title = item.name,
                        address = item.address,
                        pricePerNight = item.pricePerNight,
                        imageUrl = item.imageUrl,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.selectHotel(item.id)
                            navController.navigate("hotel/detail")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerSheet(
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    monthLabel: String,
    calendar: @Composable () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) { Text("<") }
            Text(text = monthLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNextMonth) { Text(">") }
        }

        calendar()

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("적용하기", color = Color.White)
        }
    }
}

@Composable
fun RoomPriceCard(
    title: String,
    address: String,
    pricePerNight: Int,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val fullUrl = resolveImageUrl(imageUrl) // 이 페이지 내부 헬퍼로 절대 URL 변환

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(fullUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                placeholder = painterResource(R.drawable.pp_logo),
                error = painterResource(R.drawable.pp_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = AppTypography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(address, style = AppTypography.bodySmall, color = Color(0xFF8E8E8E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(formatKrw(pricePerNight), style = AppTypography.titleSmall, color = PrimaryColor, modifier = Modifier.alignByBaseline())
                    Spacer(Modifier.weight(1f))
                    Text("/1박", style = AppTypography.bodySmall, color = Color(0xFF9DA3AE), modifier = Modifier.alignByBaseline())
                }
            }
        }
    }
}

private fun formatKrw(price: Int): String =
    NumberFormat.getCurrencyInstance(Locale.KOREA).apply {
        maximumFractionDigits = 0
    }.format(price)
