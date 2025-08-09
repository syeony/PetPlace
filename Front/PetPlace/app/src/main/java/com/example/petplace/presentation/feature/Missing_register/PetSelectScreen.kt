package com.example.petplace.presentation.feature.missing_register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Orange   = Color(0xFFFFA500)
private val GrayBtn  = Color(0xFFE9E9E9)
private val CardBorder = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySelectScreen(
    navController: NavController,
    viewModel: PetSelectViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    /* 상태 수집 */
    val pets       by viewModel.pets.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()

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

        Column(Modifier.padding(pad).padding(horizontal = 16.dp)) {

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
                            Image(
                                painterResource(pet.imageRes),
                                null,
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(pet.breed, fontSize = 12.sp, color = Color.Gray)
                                Text(pet.genderAge, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.selectPet(pet.id) },          // ← ViewModel 호출
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedId == pet.id) Orange else GrayBtn,
                                contentColor   = if (selectedId == pet.id) Color.White else Color.DarkGray
                            )
                        ) { Text("선택", fontSize = 14.sp) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            /* 확인 버튼 */
            Button(
                onClick  = { navController.navigateUp() },
                enabled  = selectedId != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor        = Orange,
                    disabledContainerColor= GrayBtn
                )
            ) { Text("확인", color = Color.White, fontSize = 16.sp) }
        }
    }
}
