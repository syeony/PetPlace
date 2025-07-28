package com.example.petplace.presentation.feature.join

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.BackgroundSoft
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class) //텍스트필드 배경색넣는것
@Composable
fun JoinScreen(navController: NavController) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var locationVerified by remember { mutableStateOf(false) }
    var agreeAll by remember { mutableStateOf(false) }
    var agreeService by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeMarketing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val imageUri = remember { mutableStateOf<Uri?>(null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri.value = uri
        }

        Box(modifier = Modifier.size(120.dp)) {

            // 프로필 이미지 배경
            Surface(
                modifier = Modifier
                    .size(120.dp),
                shape = CircleShape,
                color = Color(0xFFE0E0E0)
            ) {
                if (imageUri.value != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri.value),
                        contentDescription = "선택한 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mypage),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        tint = Color.Gray
                    )
                }
            }

            // 카메라 버튼
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .clickable { launcher.launch("image/*") }, // 클릭 이벤트 추가
                shape = CircleShape,
                color = Color(0xFFFF9800),
                shadowElevation = 4.dp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery),
                    contentDescription = "갤러리 열기",
                    tint = Color.White,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("아이디를 입력하세요") },
                modifier = Modifier.weight(1f) ,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = BackgroundSoft
                )
            )

            Button(
                onClick = { /* 중복 확인 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("중복확인", color = Color.White)
            }
        }


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호를 입력하세요") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = BackgroundSoft
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("닉네임 입력하세요") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = BackgroundSoft
                )
            )

            Button(
                onClick = { /* 중복 확인 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("중복확인", color = Color.White)
            }
        }

        Button(
            onClick = { /* 본인 인증 */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("본인 인증", color = Color.White)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("현재 위치로 인증", fontWeight = FontWeight.Bold)
                Text("GPS를 통해 동네를 인증합니다", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { locationVerified = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))) {
                    Text("현재 위치 인증하기", color = Color.Black)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreeAll, onCheckedChange = {
                agreeAll = it
                agreeService = it
                agreePrivacy = it
                agreeMarketing = it
            })
            Text("전체 동의")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreeService, onCheckedChange = { agreeService = it })
            Text("[필수] 서비스 이용약관")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreePrivacy, onCheckedChange = { agreePrivacy = it })
            Text("[필수] 개인정보 처리방침")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreeMarketing, onCheckedChange = { agreeMarketing = it })
            Text("[선택] 마케팅 정보 수신")
        }

        Button(
            onClick = { /* 회원가입 처리 */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("회원가입", color = Color.White)
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFEF9F0
)
@Composable
fun JoinPreview() {
    JoinScreen(navController = rememberNavController())
}
