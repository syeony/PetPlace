package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.petplace.presentation.common.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    var petName by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var showBreedMenu by remember { mutableStateOf(false) }
    val breedOptions = listOf("푸들", "말티즈", "시바견", "골든 리트리버")

    var gender by remember { mutableStateOf<String?>(null) }
    var neutered by remember { mutableStateOf(false) }
    var birthDate by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                "강아지 프로필 등록",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // 오른쪽 여백 맞추기
        }

        Spacer(Modifier.height(16.dp))

        // 프로필 사진
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F1F1))
                .clickable { /* 사진 선택 */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "사진 선택",
                tint = Color.Gray,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("얼굴이 잘 보이는 무보정 단독 사진을 선택해주세요.", textAlign = TextAlign.Center)

        Spacer(Modifier.height(24.dp))

        // 반려동물 이름
        OutlinedTextField(
            value = petName,
            onValueChange = { petName = it },
            placeholder = { Text("이름을 입력해주세요") },
            label = { Text("반려동물 이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 견종 선택
        ExposedDropdownMenuBox(
            expanded = showBreedMenu,
            onExpandedChange = { showBreedMenu = it }
        ) {
            OutlinedTextField(
                value = breed,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("견종을 선택해주세요") },
                label = { Text("견종 선택") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedMenu)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showBreedMenu,
                onDismissRequest = { showBreedMenu = false }
            ) {
                breedOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            breed = option
                            showBreedMenu = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 성별 선택
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { gender = "여아" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gender == "여아") Color(0xFFFFC0CB) else Color.LightGray
                )
            ) { Text("여아") }

            Button(
                onClick = { gender = "남아" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gender == "남아") Color(0xFF87CEFA) else Color.LightGray
                )
            ) { Text("남아") }
        }

        Spacer(Modifier.height(16.dp))

        // 중성화 여부
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("중성화했어요")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = neutered,
                onCheckedChange = { neutered = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        // 생일
        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            placeholder = { Text("mm/dd/yyyy") },
            label = { Text("생일") },
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 나이
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            placeholder = { Text("나이") },
            label = { Text("나이") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // 다음 버튼
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("다음", color = Color.White)
        }
    }
}
