package com.example.petplace.presentation.feature.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.PrimaryColor
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petplace.PetPlaceApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: Long,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dividerColor = Color(0xFFE5E7EB)

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val currentUserId = app.getUserInfo()?.userId ?: 0
    val isMyProfile = uiState.userProfile.userId == currentUserId

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    LaunchedEffect(uiState.createdChatRoomId) {
        uiState.createdChatRoomId?.let { chatRoomId ->
            navController.navigate("chatDetail/$chatRoomId")
            viewModel.consumeCreatedChatRoomId()
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "프로필",
                            style = AppTypography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
                Divider(color = dividerColor, thickness = 1.dp)
            }
        }
    ) { paddingValues ->

        // Loading 처리
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 프로필 섹션
            item {
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
                                    val profileImageUrl =
                                        if (!uiState.userProfile.userImgSrc.isNullOrEmpty()) {
                                            if (uiState.userProfile.userImgSrc.startsWith("http")) {
                                                uiState.userProfile.userImgSrc
                                            } else {
                                                "http://43.201.108.195:8081${uiState.userProfile.userImgSrc}"
                                            }
                                        } else null

                                    AsyncImage(
                                        model = profileImageUrl,
                                        contentDescription = "프로필 이미지",
                                        placeholder = painterResource(id = R.drawable.ic_mypage),
                                        error = painterResource(id = R.drawable.ic_mypage),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = uiState.userProfile.nickname,
                                        style = AppTypography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = uiState.userProfile.location,
                                        style = AppTypography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
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
                                    text = "${uiState.userProfile.level} Lv.",
                                    style = AppTypography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PrimaryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = uiState.userProfile.experienceProgress,
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
                            text = uiState.userProfile.introduction,
                            style = AppTypography.bodyMedium,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 채팅하기 버튼
                        if (!isMyProfile) {
                            Button(
                                onClick = {
                                    viewModel.startChatWithUser(uiState.userProfile.userId)
                                },
                                enabled = !uiState.isChatRoomCreating,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isChatRoomCreating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (uiState.isChatRoomCreating) "채팅방 생성 중..." else "채팅하기",
                                    style = AppTypography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Divider(color = dividerColor, thickness = 1.dp)
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
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 반려동물이 없을 때
                        if (uiState.pets.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .border(
                                        2.dp,
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "등록된 반려동물이 없습니다.",
                                    style = AppTypography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            // 반려동물이 있을 때는 각 반려동물마다 카드 표시
                            uiState.pets.forEachIndexed { index, pet ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                UserPetInfoCard(pet = pet)
                            }
                        }
                    }
                }
                Divider(color = dividerColor, thickness = 1.dp)
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 목욕 용품
                            UserPetSupplyItem(
                                imageRes = R.drawable.bath_item,
                                title = "목욕 용품",
                                actualImageUrl = uiState.petSupplies.bathImageUrl
                            )

                            // 사료 용품
                            UserPetSupplyItem(
                                imageRes = R.drawable.bowl_item,
                                title = "사료 용품",
                                actualImageUrl = uiState.petSupplies.foodImageUrl
                            )

                            // 배변 용품
                            UserPetSupplyItem(
                                imageRes = R.drawable.poo_item,
                                title = "배변 용품",
                                actualImageUrl = uiState.petSupplies.wasteImageUrl
                            )
                        }
                    }
                }
                Divider(color = dividerColor, thickness = 1.dp)
            }

            // 산책/돌봄 후기 섹션
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
                            text = "산책/돌봄 후기",
                            style = AppTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UserWalkCareMenuItem(
                                icon = R.drawable.my_walk,
                                title = "산책 후기",
                                onClick = {
//                                    navController.navigate("walk_reviews/${uiState.userProfile.userId}")
                                }
                            )
                            UserWalkCareMenuItem(
                                icon = R.drawable.my_care,
                                title = "돌봄 후기",
                                onClick = {
//                                    navController.navigate("care_reviews/${uiState.userProfile.userId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPetInfoCard(
    pet: UserPetInfo
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(
                2.dp,
                Color(0xFFE0E0E0),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            ) {
                val petImageUrl = if (!pet.imgSrc.isNullOrEmpty()) {
                    if (pet.imgSrc.startsWith("http")) {
                        pet.imgSrc
                    } else {
                        "http://43.201.108.195:8081${pet.imgSrc}"
                    }
                } else null

                AsyncImage(
                    model = petImageUrl,
                    contentDescription = "반려동물 이미지",
                    placeholder = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24),
                    error = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = pet.name,
                    style = AppTypography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = pet.breed,
                    style = AppTypography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "${pet.gender} ${pet.age}살",
                    style = AppTypography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// 용품 아이템 컴포저블 (상호작용 없음)
@Composable
fun UserPetSupplyItem(
    imageRes: Int,
    title: String,
    actualImageUrl: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            // 실제 이미지가 있으면 그것을 사용, 없으면 기본 아이콘 사용
            if (!actualImageUrl.isNullOrEmpty()) {
                // 사용자가 업로드한 실제 이미지
                AsyncImage(
                    model = if (actualImageUrl.startsWith("http"))
                        actualImageUrl
                    else
                        "http://43.201.108.195:8081$actualImageUrl",
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = imageRes),
                    error = painterResource(id = imageRes)
                )
            } else {
                // 기본 아이콘
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = AppTypography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun UserWalkCareMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
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
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = AppTypography.bodySmall,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    val mockUiState = UserProfileUiState(
        userProfile = UserProfileInfo(
            userId = 1,
            nickname = "이도형",
            location = "인의동",
            level = 1,
            experienceProgress = 0.7f,
            introduction = "안녕하세요!\n저는 펫샵에 갔어지에 관심이 많아서 물렁물렁 많이 해보고 싶어하게 되었습니다 더보기...",
            userImgSrc = ""
        ),
        pets = listOf(
            UserPetInfo(
                id = 1,
                name = "두부",
                breed = "말티즈",
                gender = "여아",
                age = 8,
                imgSrc = ""
            )
        ),
        petSupplies = UserPetSupplies(
            bathImageUrl = "sample_bath_url",
            foodImageUrl = "sample_food_url",
            wasteImageUrl = "sample_waste_url"
        ),
        isLoading = false,
        error = null
    )

    // Mock ViewModel을 만들어서 프리뷰에서 사용
    UserProfileScreen(
        navController = rememberNavController(),
        userId = 1
    )
}