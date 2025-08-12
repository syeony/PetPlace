package com.example.petplace.presentation.feature.hotel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationSuccessScreen(
    navController: NavController,
    merchantUid: String,
    reservationId: Long? = null
) {
    val clipboard = LocalClipboardManager.current
    val snack = remember { SnackbarHostState() }
    val now = remember { LocalDateTime.now() }
    val timeText = remember { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("결제 완료") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snack) },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // 보유한 라우트에 맞게 변경 가능
                        navController.navigate("nav_home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("홈으로") }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("닫기") }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "결제가 완료되었어요!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "예약이 정상적으로 접수되었습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // 정보 카드
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow(label = "주문번호(merchantUid)", value = merchantUid, onCopy = {
                        clipboard.setText(AnnotatedString(merchantUid))
                        LaunchedEffect(Unit) {
                            snack.showSnackbar("주문번호를 복사했어요")
                        }
                    })
                    Spacer(Modifier.height(12.dp))
                    if (reservationId != null) {
                        InfoRow(label = "예약번호", value = reservationId.toString(), onCopy = {
                            clipboard.setText(AnnotatedString(reservationId.toString()))
                            LaunchedEffect(Unit) {
                                snack.showSnackbar("예약번호를 복사했어요")
                            }
                        })
                        Spacer(Modifier.height(12.dp))
                    }
                    InfoRow(label = "결제일시", value = timeText)
                    Spacer(Modifier.height(12.dp))
                    InfoRow(label = "상태", value = "결제완료 / 예약접수")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "마이페이지 > 예약 내역에서 상세를 확인할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    onCopy: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
//        if (onCopy != null) {
//            Spacer(Modifier.width(8.dp))
//            TextButton(onClick = onCopy) { Text("복사") }
//        }
    }
}
