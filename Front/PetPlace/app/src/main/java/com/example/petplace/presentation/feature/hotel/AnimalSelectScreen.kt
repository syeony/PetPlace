package com.example.petplace.presentation.feature.hotel

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
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalSelectScreen(
    navController: NavController,
    viewModel: HotelSharedViewModel
) {
    var selected by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center // 세로 중앙 정렬
                ) {
                    Text(
                        "펫호텔",
                        style = AppTypography.titleMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = BackgroundColor
            ),
            modifier = Modifier.height(48.dp), // 높이 줄이기
            windowInsets = WindowInsets(0.dp)  // 상단 패딩 제거
        )

    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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
                    navController.navigate("hotel/date")

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
                    navController.navigate("hotel/date")
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
