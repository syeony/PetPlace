package com.example.petplace.presentation.feature.hotel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.PetPlaceApp
import com.example.petplace.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationCheckoutScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel = hiltViewModel()
) {
    val reservation by viewModel.reservationState.collectAsState()
    val detail by viewModel.hotelDetail.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(reservation.selectedHotelId) {
        reservation.selectedHotelId?.let { viewModel.getHotelDetail() }
    }

    // 날짜 텍스트 (날짜만 표시)
    val checkInText = reservation.checkInDate ?: "-"      // 예: "2025-05-27"
    val checkOutText = reservation.checkOutDate ?: "-"    // 예: "2025-05-29"

    // 특이사항(요청사항) — UI 로컬 상태로만 유지(연결 X)
    var specialRequest by rememberSaveable { mutableStateOf("") }
    // var specialRequest by rememberSaveable { mutableStateOf(reservation.specialRequests ?: "") }
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                KakaoPayButton(
                    enabled = detail != null,
                    onClick = {
                        detail?.let { navController.navigate("hotel/reserve/${it.id}") }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            if (error != null) {
                Text(text = error ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
            }

            // 상단 이미지
            Card(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                AsyncImage(
                    model = detail?.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.pp_logo),
                    error = painterResource(R.drawable.pp_logo),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 이름/주소
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = detail?.name ?: "로딩 중…",
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = detail?.address ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 체크인/체크아웃 (날짜만)
            SectionCard(title = "체크인 / 체크아웃") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DateItem(label = "체크인", value = checkInText, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    DateItem(label = "체크아웃", value = checkOutText, modifier = Modifier.weight(1f))
                }
            }
            SectionCard(title = "유저 정보") {
                Column(Modifier.fillMaxWidth()) {
                    userInfo?.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?.let { name ->
                            DateItem(
                                label = "이름",
                                value = name,                 // weight 제거
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }

            // 특이사항(요청사항)
            SectionCard(title = "특이사항") {
                OutlinedTextField(
                    value = specialRequest,
                    onValueChange = { specialRequest = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    placeholder = { Text("예) 창가 쪽 방으로 부탁드려요.") },
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            // 사진 설명(호텔 소개)
            if (!detail?.description.isNullOrBlank()) {
                SectionCard(title = "사진 설명") {
                    Text(text = detail!!.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(90.dp)) // 바텀 버튼 영역 여백
        }
    }
}

/** 공통 섹션 카드 */
@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun DateItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(end = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEE9B00)) // 강조색
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

/** 카카오페이 스타일 버튼 */
@Composable
private fun KakaoPayButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFEE500),
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFF3F3F3),
            disabledContentColor = Color(0xFF9E9E9E)
        ),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = if (enabled) "pay 로 결제하기" else "불러오는 중…")
        }
    }
}
