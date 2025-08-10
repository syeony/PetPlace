package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.PrimaryColor

@Composable
fun MyPageScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "프로필",
                style = AppTypography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        // 프로필 섹션
        item {
            Divider(color = Color.LightGray, thickness = 1.dp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 프로필 이미지
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_mypage), // 실제 이미지로 교체
                                    contentDescription = "프로필 이미지",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = userInfo?.nickname.toString(),
                                    style = AppTypography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "인의동",
                                    style = AppTypography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(onClick = {
                            navController.navigate("profile_edit")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "이동",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 경험치 바
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "꼬순내지수",
                                style = AppTypography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "1 Lv.",
                                style = AppTypography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PrimaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = 0.6f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryColor,
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "안녕하세요!",
                        style = AppTypography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "안녕하세요!\n" +
                                "저는 평소에 강아지에 관심이 많아서 돌봄을 많이 해 보고 싶어서 가입하게 되었습니다!\n" +
                                "돌봄이 필요할때 언제든 연락주세요! 그리고... 더보기",
                        style = AppTypography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

        // 내 가족 섹션
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "내 가족",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 반려동물 추가 버튼
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(
                                2.dp,
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { navController.navigate("pet_profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "추가",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "반려동물 추가",
                                style = AppTypography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 두부 (반려동물)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(
                                2.dp,
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                            .clickable { /* 반려동물 정보 로직 */ }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24), // 실제 이미지로 교체
                                        contentDescription = "두부",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "두부",
                                        style = AppTypography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "말티즈",
                                        style = AppTypography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "여아 8살",
                                        style = AppTypography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    // 정보수정
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "정보수정",
                                    color = Color.Black,
                                    style = AppTypography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

        // 펫 용품 섹션
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "펫 용품",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 펫 용품 이미지들
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE3F2FD))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ball), // 실제 이미지로 교체
                                    contentDescription = "펫 용품",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

        // MY 섹션
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "MY",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // MY 메뉴들
                        MyMenuItem(
                            icon = R.drawable.my_post,
                            title = "내 게시글",
                            onClick = { navController.navigate("my_post") }
                        )
                        MyMenuItem(
                            icon = R.drawable.my_comment,
                            title = "내 댓글",
                            onClick = { /* 댓글 로직 */ }
                        )
                        MyMenuItem(
                            icon = R.drawable.heart,
                            title = "찜한글",
                            onClick = { /* 찜한글 로직 */ }
                        )
                    }
                }
            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

        // 산책/돌봄 섹션
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "산책/돌봄",
                        style = AppTypography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MyMenuItem(
                            icon = R.drawable.my_walk,
                            title = "내 산책",
                            onClick = { /* 산책 로직 */ }
                        )
                        MyMenuItem(
                            icon = R.drawable.my_care,
                            title = "내 돌봄",
                            onClick = { /* 돌봄 로직 */ }
                        )
                    }
                }
            }
        }

        // 로그아웃 버튼
        item {
            Button(
                onClick = {
                    app.clearLoginData()
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "로그아웃",
                    style = AppTypography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MyMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    // 아이콘 배경 박스
    Box(
        modifier = Modifier
            .size(80.dp) // 아이콘 영역 크기
            .clip(RoundedCornerShape(12.dp)) // 둥근 사각형
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onClick() }
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(40.dp) // 아이콘 크기
            )
            Spacer(modifier = Modifier.height(6.dp))

            // 메뉴 이름
            Text(
                text = title,
                style = AppTypography.bodySmall,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
        }
    }
}

