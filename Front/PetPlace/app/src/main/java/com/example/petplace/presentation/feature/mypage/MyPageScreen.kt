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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.BeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // val app = PetPlaceApp.getAppContext() as PetPlaceApp
    // val userInfo = app.getUserInfo()
    val dividerColor = Color(0xFFE5E7EB)

    // Error 처리
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 표시 로직 (SnackBar 등)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
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
                .padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 프로필 섹션
            item {
                Divider(color = dividerColor, thickness = 1.dp)
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
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(120.dp)
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
                        // 반려동물 목록이 비어있을 때는 추가 버튼만 표시
                        if (uiState.pets.isNotEmpty()) {
                            // 반려동물이 있을 때는 각 반려동물마다 카드 표시
                            uiState.pets.forEachIndexed { index, pet ->
                                Spacer(modifier = Modifier.height(12.dp))


                                PetInfoCard(
                                    pet = pet,
                                    onEditClick = { petId ->
                                        navController.navigate("pet_profile?petId=$petId")
                                    },
                                    onCardClick = {
                                        // 반려동물 상세 정보 로직
                                    }
                                )
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
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 목욕 용품
                            PetSupplyItem(
                                imageRes = R.drawable.bath_item,
                                title = "목욕 용품",
                                actualImageUrl = uiState.petSupplies.bathImageUrl,
                                onClick = {
                                    viewModel.showSupplyDialog(SupplyType.BATH)
                                }
                            )

                            // 사료 용품
                            PetSupplyItem(
                                imageRes = R.drawable.bowl_item,
                                title = "사료 용품",
                                actualImageUrl = uiState.petSupplies.foodImageUrl,
                                onClick = {
                                    viewModel.showSupplyDialog(SupplyType.FOOD)
                                }
                            )

                            // 배변 용품
                            PetSupplyItem(
                                imageRes = R.drawable.poo_item,
                                title = "배변 용품",
                                actualImageUrl = uiState.petSupplies.wasteImageUrl,
                                onClick = {
                                    viewModel.showSupplyDialog(SupplyType.WASTE)
                                }
                            )
                        }
                    }
                }
                Divider(color = dividerColor, thickness = 1.dp)
                // 용품 관리 다이얼로그
                if (uiState.showSupplyDialog) {
                    PetSupplyDialog(
                        supplyType = uiState.currentSupplyType,
                        selectedImage = uiState.selectedSupplyImage,
                        existingImageUrl = uiState.currentSupplyType?.let {
                            viewModel.getExistingSupplyImageUrl(it)
                        },
                        isSaving = uiState.isSavingSupply,
                        onDismiss = { viewModel.hideSupplyDialog() },
                        onImageSelected = { uri -> viewModel.updateSupplyImage(uri) },
                        onConfirm = { viewModel.saveSupplyInfo() }
                    )
                }
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
                                title = "내 피드",
                                onClick = { navController.navigate("my_post") }
                            )
                            MyMenuItem(
                                icon = R.drawable.my_comment,
                                title = "내 댓글",
                                onClick = { navController.navigate("my_comment") }
                            )
                            MyMenuItem(
                                icon = R.drawable.heart,
                                title = "찜한글",
                                onClick = { navController.navigate("my_likePost") }
                            )
                        }
                    }
                }
                Divider(color = dividerColor, thickness = 1.dp)
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
                            WalkCareMenuItem(
                                icon = R.drawable.my_walk,
                                title = "내 산책",
                                onClick = { navController.navigate("my_walk") }
                            )
                            WalkCareMenuItem(
                                icon = R.drawable.my_care,
                                title = "내 돌봄",
                                onClick = { navController.navigate("my_care") }
                            )
                        }
                    }
                }
            }

            // 로그아웃 버튼
            item {
                Button(
                    onClick = {
                        viewModel.logout()
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
        }
    }
}

@Composable
fun PetInfoCard(
    pet: PetInfo,
    onEditClick: (Int) -> Unit,
    onCardClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(150.dp)
            .border(
                2.dp,
                Color(0xFFE0E0E0),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .clickable { onCardClick() }
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

                Column {
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onEditClick(pet.id) },
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

@Composable
fun MyMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    // 아이콘 배경 박스
    Box(
        modifier = Modifier
            .size(90.dp) // 아이콘 영역 크기
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

@Composable
fun WalkCareMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    // 아이콘 배경 박스
    Box(
        modifier = Modifier
            .height(100.dp)
            .width(120.dp)
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

// 용품 아이템 컴포저블
@Composable
fun PetSupplyItem(
    imageRes: Int,
    title: String,
    actualImageUrl: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
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
                error = painterResource(id = imageRes),
                onError = { error ->
                    Log.e("PetSupplyItem", "이미지 로드 실패: $actualImageUrl", error.result.throwable)
                },
                onSuccess = {
                    Log.d("PetSupplyItem", "이미지 로드 성공: $actualImageUrl")
                }
            )

        } else {
            // 기본 아이콘
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                // 기본 이미지
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // 중앙에 하얀색 플러스 아이콘
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "미등록",
                    tint = Color.White, // 하얀색
                    modifier = Modifier.size(32.dp) // 원하는 크기
                )
            }
        }
    }
}

// 용품 관리 다이얼로그
@Composable
fun PetSupplyDialog(
    supplyType: SupplyType?,
    selectedImage: Uri? = null,
    existingImageUrl: String? = null, // 기존 이미지 URL 추가
    isSaving: Boolean = false, // 저장 상태 추가
    onDismiss: () -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    val supplyInfo = when (supplyType) {
        SupplyType.BATH -> Triple(
            "목욕 용품",
            if (existingImageUrl.isNullOrEmpty()) "목욕 용품 사진을 올려주세요." else "목욕 용품 사진을 변경하세요.",
            R.drawable.bath_item
        )

        SupplyType.FOOD -> Triple(
            "사료 용품",
            if (existingImageUrl.isNullOrEmpty()) "사료 사진을 올려주세요." else "사료 사진을 변경하세요.",
            R.drawable.bowl_item
        )

        SupplyType.WASTE -> Triple(
            "배변 용품",
            if (existingImageUrl.isNullOrEmpty()) "배변 용품 사진을 올려주세요." else "배변 용품 사진을 변경하세요.",
            R.drawable.poo_item
        )

        else -> Triple("용품", "용품 사진을 올려주세요.", R.drawable.bath_item)
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() }, // 저장 중일 때는 닫기 방지
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 용품 이미지
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable(enabled = !isSaving) {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // 새로 선택된 이미지가 있을 때
                        selectedImage != null -> {
                            AsyncImage(
                                model = selectedImage,
                                contentDescription = supplyInfo.first,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // 기존 이미지가 있을 때
                        !existingImageUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = if (existingImageUrl.startsWith("http"))
                                    existingImageUrl
                                else
                                    "http://43.201.108.195:8081$existingImageUrl", // 서버 베이스 URL 추가
                                contentDescription = supplyInfo.first,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = supplyInfo.third),
                                error = painterResource(id = supplyInfo.third),
                                onError = { error ->
                                    Log.e(
                                        "AsyncImage",
                                        "기존 이미지 로드 실패: $existingImageUrl",
                                        error.result.throwable
                                    )
                                },
                                onSuccess = {
                                    Log.d("AsyncImage", "기존 이미지 로드 성공: $existingImageUrl")
                                }
                            )
                        }
                        // 기본 이미지
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center // 중앙 정렬
                            ) {
                                Image(
                                    painter = painterResource(id = supplyInfo.third),
                                    contentDescription = supplyInfo.first,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "미등록",
                                    tint = Color.White, // 하얀색
                                    modifier = Modifier.size(32.dp) // 원하는 크기
                                )
                            }
                        }
                    }

                    // 저장 중 로딩 표시
                    if (isSaving) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = supplyInfo.second,
                    style = AppTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                // 기존 이미지가 있을 때 상태 표시
                if (!existingImageUrl.isNullOrEmpty() && selectedImage == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "현재 등록된 ${supplyInfo.first}",
                        style = AppTypography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isSaving) {
                        onConfirm()
                    }
                },
                enabled = !isSaving && (selectedImage != null || !existingImageUrl.isNullOrEmpty()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "저장 중...",
                            color = Color.White,
                            style = AppTypography.labelLarge
                        )
                    }
                } else {
                    Text(
                        text = if (existingImageUrl.isNullOrEmpty()) "등록" else "수정",
                        color = Color.White,
                        style = AppTypography.labelLarge
                    )
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(16.dp)
    )
}

// 용품 타입 enum
enum class SupplyType {
    BATH, FOOD, WASTE
}