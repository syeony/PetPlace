package com.example.petplace.presentation.feature.Missing_register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R

private val BgColor      = Color(0xFFFEF9F0)
private val AccentOrange = Color(0xFFFFA500)   // 하단 버튼색

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen() {

    // 간단한 상태(실제 기능 연결 전 임시용)
    var detail by remember { mutableStateOf("") }
    var date   by remember { mutableStateOf("2024년 01월 15일") }
    var time   by remember { mutableStateOf("오후 14:30") }
    var place  by remember { mutableStateOf("경상북도 구미시 인의동 365-5") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실종 신고") },
                navigationIcon = {
                    IconButton(onClick = { /* navController?.popBackStack() */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgColor
                )
            )
        },
        containerColor = BgColor
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            // 🐶 반려동물 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 예시용 이미지
                    Image(
                        painter = painterResource(R.drawable.pp_logo), // 프로젝트 리소스
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("코코", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("골든 리트리버 • 3살", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            // 📸 사진 업로드 자리
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(top = 24.dp)
                    .border(
                        BorderStroke(1.dp, Color(0xFFD7D7D7)),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_photo_camera_24),
                        contentDescription = null,               // 접근성 설명 필요 시 넣어주세요
                        tint = Color(0xFF8C8C8C)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("1 / 5", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C),
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            // ✍️ 상세 내용 입력
            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                placeholder = { Text("실종 장소, 상황, 특징 등을 작성해주세요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            // 실종 일시
            Text("실종 일시", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        disabledTextColor = Color.Black,
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        disabledTextColor = Color.Black,
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // 실종 장소
            Text("실종 장소", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = place,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    disabledTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(Modifier.height(4.dp))
            Text(
                "작성 완료 후에는 장소를 변경할 수 없어요.",
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C)
            )

            Spacer(Modifier.height(40.dp))

            // 완료 버튼
            Button(
                onClick = { /* TODO: 저장 후 이동 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("작성완료", color = Color.White, fontSize = 16.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
