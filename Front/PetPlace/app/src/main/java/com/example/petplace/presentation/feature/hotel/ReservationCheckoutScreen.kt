package com.example.petplace.presentation.feature.hotel

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.petplace.BuildConfig
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.Iamport
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationCheckoutScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel = hiltViewModel()
) {
    val reservation by viewModel.reservationState.collectAsState()
    val detail by viewModel.hotelDetail.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current as ComponentActivity
    val scope = rememberCoroutineScope()

    LaunchedEffect(reservation.selectedHotelId) {
        reservation.selectedHotelId?.let { viewModel.getHotelDetail() }
    }

    // 날짜 텍스트
    val checkInText = reservation.checkInDate ?: "-"
    val checkOutText = reservation.checkOutDate ?: "-"

    // 특이사항(로컬 상태)
    var specialRequest by rememberSaveable { mutableStateOf("") }
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    // ===== 결제 유틸 =====
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    fun calcNights(from: String?, to: String?): Int = try {
        val start = LocalDate.parse(from, dateFmt)
        val end = LocalDate.parse(to, dateFmt)
        max(1, ChronoUnit.DAYS.between(start, end).toInt())
    } catch (_: Throwable) { 1 }

    fun calcAmount(): Int {
        val pricePerNight = detail?.pricePerNight ?: 0
        val nights = calcNights(reservation.checkInDate, reservation.checkOutDate)
        return pricePerNight * nights
    }


    // (권장: 서버발급) 데모용 merchantUid
    fun newMerchantUid(): String = "pp_${System.currentTimeMillis()}_${(1000..9999).random()}"
    fun startKakaoPay(
        userCode: String,
        merchantUid: String,
        amount: Int,
        buyerName: String?,
        buyerTel: String?,
        onSuccess: suspend (impUid: String, merchantUid: String) -> Unit
    ) {
        val req = IamPortRequest(
            pg = "kakaopay",
            pay_method = PayMethod.card.name,
            name = detail?.name ?: "호텔 예약 결제",
            merchant_uid = merchantUid,
            amount = amount.toString(),
            buyer_name = buyerName,
            buyer_tel = buyerTel,
            app_scheme = "iamportapp"
        )

        Iamport.payment(
            userCode = userCode,
            iamPortRequest = req,
            paymentResultCallback = { result ->
                android.util.Log.d("PAY", "result=$result success=${result?.success} msg=${result?.error_msg}")
                scope.launch {
                    if (result?.imp_success == true) {
                        val impUid = result.imp_uid ?: return@launch
                        val mUid = result.merchant_uid ?: return@launch
                        onSuccess(impUid, mUid)
                    } else {
                        // 실패/취소 사유확인: result?.error_msg
                    }
                }
            }
        )

    }







    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center // 세로 중앙 정렬
                    ) {
                        Text(
                            "예약 내용",
                            style = AppTypography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.height(48.dp), // 높이 줄이기
                windowInsets = WindowInsets(0.dp)  // 상단 패딩 제거
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,        // ✅ 바텀바 배경 화이트
                tonalElevation = 0.dp,      // ✅ 톤 오버레이 제거 (순수한 흰색 유지)
                shadowElevation = 4.dp      // ✅ 분리감은 그림자로
            ) {
                KakaoPayButton(
                    enabled = detail != null,
                    onClick = {
                        scope.launch {
                            // 1) 예약 생성
                            val reservationId = viewModel.makeHotelReservation() ?: return@launch

                            // 2) 결제 준비(서버에서 merchantUid/amount 받기)
                            val prep = viewModel.preparePayment(reservationId) ?: return@launch

                            // 3) 결제창 오픈 (성공 후 후속 처리 전달)
                            startKakaoPay(
                                userCode = BuildConfig.IMP_KEY,
                                merchantUid = prep.merchantUid,
                                amount = prep.amount,
                                buyerName = userInfo?.nickname,
                                buyerTel = userInfo?.phoneNumber
                            ) { impUid, mUid ->
                                // 4) 결제 검증
                                Log.d("PAY", "$impUid $mUid")
//                                val ok = viewModel.verifyPayment(impUid, mUid)
//                                if (ok) {
//                                    // 5) 예약 확정
//                                    viewModel.confirmReservation(reservationId)
//
//                                    // (선택) 성공 화면 이동
//                                     navController.navigate("hotel/success")
//                                } else {
//                                    // 검증 실패 알림 처리
//
//                                }
                                // 5) 예약 확정
//                                    viewModel.confirmReservation(reservationId)

                                    // (선택) 성공 화면 이동

//                                navController.navigate("payment/wait/${mUid}?rid=$reservationId")
//                                route = "hotel/success/{merchantUid}?rid={reservationId}",

                                navController.navigate(
                                    "hotel/success/${Uri.encode(mUid)}?rid=${reservationId.toLong()}"
                                )
                            }
                        }
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),           // ✅ 카드 배경 화이트
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

            // 유저 정보
            SectionCard(title = "유저 정보") {
                Column(Modifier.fillMaxWidth()) {
                    userInfo?.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?.let { name ->
                            DateItem(
                                label = "이름",
                                value = name,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }

            // 특이사항
            SectionCard(title = "특이사항") {
                OutlinedTextField(
                    value = reservation.specialRequest,                  // ← ViewModel 값
                    onValueChange = { viewModel.updateSpecialRequest(it) }, // ← ViewModel로 반영
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    placeholder = { Text("예) 소고기 알러지가 있어요.") },
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

@Composable
private fun SectionCard(
    title: String,
    containerColor: Color = Color.White, // ✅ 기본값 White
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Card(shape = RoundedCornerShape(16.dp),        colors = CardDefaults.cardColors(containerColor = containerColor),
        ) {
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEE9B00))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

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
