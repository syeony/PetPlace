package com.example.petplace.presentation.feature.hotel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petplace.presentation.common.theme.AppTypography
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

    var startDate by remember { mutableStateOf<LocalDate?>(today) }
    var endDate by remember { mutableStateOf<LocalDate?>(today.plusDays(1)) }

    Log.d("animal", "고른 동물 :${reservationState.selectedAnimal}")

    LaunchedEffect(startDate, endDate) {
        viewModel.selectDate(
            startDate?.toString().orEmpty(),
            endDate?.toString().orEmpty()
        )
    }

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
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("날짜 선택", style = AppTypography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,          // ✅ 탑바 배경 화이트
                    scrolledContainerColor = Color.White,  // ✅ 스크롤 시에도 화이트 유지
                ),
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) {
        // 스크롤 영역 + 하단 고정 버튼 레이아웃
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(0.dp) // 원래 의도 유지
        ) {

            // ===== 스크롤 가능한 본문 =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 0.dp, vertical = 0.dp)
                    // 하단 버튼과 겹치지 않도록 여유 공간 확보
                    .padding(bottom = 88.dp)
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
                        ) { Icon(Icons.Default.ArrowBack, contentDescription = "이전 달") }

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
                        ) { Icon(Icons.Default.ArrowForward, contentDescription = "다음 달") }
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
                                        shape = RoundedCornerShape(0.dp)
                                    )
                                    .clickable(enabled = day.position == DayPosition.MonthDate) {
                                        if (startDate == null || (startDate != null && endDate != null)) {
                                            startDate = day.date
                                            endDate = null
                                        } else if (startDate != null && endDate == null) {
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
                                        viewModel.selecMyPet(pet.id, pet.animal)
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
                                    val request = ImageRequest.Builder(context)
                                        .data(pet.imgSrc.toFullImageUrl())
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
            }

            // ===== 하단 고정 버튼 =====
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
                        // TODO: 펫 선택 안내
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
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
