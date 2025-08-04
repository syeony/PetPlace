package com.example.petplace.presentation.feature.hotel

import android.content.res.Resources.Theme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundColor
import com.example.petplace.presentation.common.theme.PrimarySoft
import com.example.petplace.presentation.feature.join.JoinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalSelectScreen(
    navController: NavController
) {
    var selected by remember { mutableStateOf<String?>(null) }
    val viewModel: HotelSharedViewModel = hiltViewModel()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("펫호텔")},
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack() }){
                    Icon(Icons.Default.ArrowBack , contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor =  BackgroundColor)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "누구를 맡기시나요?",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = AppTypography.headlineLarge
            )

            Spacer(Modifier.height(24.dp))

            // 강아지 카드
            AnimalCard(
                imageRes = R.drawable.dog_square,
                label = "강아지",
                isSelected = selected == "강아지",
                onClick = {
                    selected = "강아지"
                    viewModel.selectAnimal(selected!!)
                    navController.navigate("DateSelectionScreen")

                },

                )

            Spacer(Modifier.height(16.dp))

            // 고양이 카드
            AnimalCard(
                imageRes = R.drawable.cat_square,
                label = "고양이",
                isSelected = selected == "고양이",
                onClick = {
                    selected = "고양이"
                    viewModel.selectAnimal(selected!!)
                    navController.navigate("DateSelectionScreen")
                }
            )
        }

    }

}



@Composable
fun AnimalCard(
    imageRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            2.dp,
            if (isSelected) Color(0xFFFF9800) else Color(0xFFE0E0E0)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(label, style = AppTypography.bodyLarge)
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFEF9F0
)
@Composable
fun HotelPreview() {
//    AnimalSelecScreen()
}
