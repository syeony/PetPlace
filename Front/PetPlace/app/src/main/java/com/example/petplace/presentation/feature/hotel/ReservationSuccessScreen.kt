package com.example.petplace.presentation.feature.hotel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationSuccessScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel,
    merchantUid: String,          // ← 결제 주문번호
    reservationId: Long           // ← 예약 ID (confirm에 사용)
) {
    val scope = rememberCoroutineScope()

    // WAITING / SUCCESS / FAILED
    var stage by remember { mutableStateOf("WAITING") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // 들어오자마자 결제 상태 폴링 시작 → PAID면 confirm 호출 후 SUCCESS
    LaunchedEffect(merchantUid) {
        viewModel.pollPaymentUntilSettled(
            merchantUid = merchantUid,
            onPaid = {
                // 결제가 PAID되면 서버에 예약 확정 요청
                scope.launch {
                    runCatching { viewModel.confirmReservation(reservationId) }
                        .onSuccess { stage = "SUCCESS" }
                        .onFailure { e ->
                            stage = "FAILED"
                            errorMsg = e.message ?: "예약 확정에 실패했습니다."
                        }
                }
            },
            onFinish = { info, err ->
                if (stage == "SUCCESS") return@pollPaymentUntilSettled
                when (info?.status?.uppercase()) {
                    "FAILED", "CANCELED" -> {
                        stage = "FAILED"
                        errorMsg = info.failureReason ?: "결제가 완료되지 않았습니다."
                    }
                    "PAID" -> { /* onPaid에서 처리됨 */ }
                    else -> if (err != null) {
                        stage = "FAILED"
                        errorMsg = err
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when (stage) {
                            "SUCCESS" -> "예약 성공"
                            "FAILED"  -> "결제 확인 실패"
                            else      -> "결제 확인 중…"
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            when (stage) {
                "WAITING" -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "결제 완료 여부를 확인하고 있어요.\n잠시만 기다려주세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }

                "SUCCESS" -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "예약이 완료되었습니다!",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(40.dp))
                    Button(
                        onClick = {
                            navController.navigate("nav_feed") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("메인 페이지로 가기")
                    }
                }

                "FAILED" -> {
                    Text(
                        text = errorMsg ?: "결제 확인에 실패했습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("뒤로가기") }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                // 재시도: 상태 초기화 후 다시 폴링
                                stage = "WAITING"
                                errorMsg = null
                                // LaunchedEffect(merchantUid)가 이미 있으므로 stage만 바꾸면 폴링 재개됨
                                // (필요하면 별도 retry 트리거로 viewModel.getPaymentInfoOnce() 호출 가능)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("다시 확인") }
                    }
                }
            }
        }
    }
}
