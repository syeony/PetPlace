package com.example.petplace.presentation.feature.missing_register

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

private val Orange   = Color(0xFFFFA500)
private val GrayBtn  = Color(0xFFE9E9E9)
private val CardBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySelectScreen(
    navController: NavController,
    viewModel: PetSelectViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val pets       by viewModel.pets.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()
    val loading    by viewModel.loading.collectAsState()
    val error      by viewModel.error.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("내 펫") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()) // 🔹 세로 스크롤 가능하게
        ) {
            when {
                loading -> {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error != null -> {
                    Spacer(Modifier.height(16.dp))
                    Text("불러오기에 실패했어요: $error", color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.refresh() }) { Text("다시 시도") }
                }
                else -> {
                    pets.forEach { pet ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = "http://i13d104.p.ssafy.io:8081"+pet.imgSrc,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        val sexKo = when (pet.sex.uppercase()) {
                                            "MALE" -> "남아"
                                            "FEMALE" -> "여아"
                                            else -> "성별미상"
                                        }

                                        val ageText = pet.birthday?.let { birthdayStr ->
                                            runCatching {
                                                val birthDate = java.time.LocalDate.parse(birthdayStr)
                                                Log.d("hi", birthDate.toString())
                                                val now = java.time.LocalDate.now()
                                                val years = java.time.Period.between(birthDate, now).years
                                                "${years}살"
                                            }.getOrElse { "" }
                                        } ?: ""

                                        Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(pet.breed, fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            "$sexKo ${ageText}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.selectPet(pet.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedId == pet.id) Orange else GrayBtn,
                                        contentColor = if (selectedId == pet.id) Color.White else Color.DarkGray
                                    )
                                ) { Text("선택", fontSize = 14.sp) }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val id = selectedId
                            if (id != null) {
                                val pet = pets.firstOrNull { it.id == id }
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.apply {
                                        set("pet_id",      pet?.id)          // ✅ 이 줄 추가
                                        set("pet_name",     pet?.name)
                                        set("pet_breed",    pet?.breed)
                                        set("pet_sex",      pet?.sex)         // "MALE"/"FEMALE"/기타
                                        set("pet_birthday", pet?.birthday)    // "yyyy-MM-dd" or null
                                        set("pet_img",      pet?.imgSrc)      // "/path" 형태면 Register에서 base붙임
                                    }
                            }
                            navController.navigateUp()
                        },
                        enabled = selectedId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange,
                            disabledContainerColor = GrayBtn
                        )
                    ) { Text("확인", color = Color.White, fontSize = 16.sp) }
                }
            }
        }
    }
}
