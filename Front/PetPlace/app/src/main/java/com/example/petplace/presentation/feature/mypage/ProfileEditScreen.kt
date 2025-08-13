package com.example.petplace.presentation.feature.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.BackgroundColor
import com.example.petplace.presentation.common.theme.PrimaryColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val amber = Color(0xFFFFC981)
    val bg = Color.White

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // 갤러리에서 이미지 선택 런처
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.updateProfileImage(uri)
            }
        }
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 에러 표시 로직
            viewModel.clearMessages()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // 성공 메시지 표시 로직
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // 커스텀 상단 바
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "프로필 수정",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // 프로필 이미지 + 카메라 오버레이
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(112.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val imageToShow = when {
                            // 새로 선택한 이미지가 있으면 우선 표시
                            uiState.profileImageUri != null -> uiState.profileImageUri
                            // 서버 URL이 있으면 표시 (full URL 처리)
                            !uiState.profileImageUrl.isNullOrEmpty() -> {
                                if (uiState.profileImageUrl!!.startsWith("http")) {
                                    uiState.profileImageUrl
                                } else {
                                    "http://43.201.108.195:8081${uiState.profileImageUrl}" // 서버 베이스 URL 추가
                                }
                            }

                            else -> null
                        }

                        if (imageToShow != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageToShow),
                                contentDescription = "프로필 이미지",
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_mypage),
                                contentDescription = "기본 프로필 이미지",
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Surface(
                            color = PrimaryColor,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .offset(x = (-5).dp, y = (-5).dp)
                                .size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            launcherGallery.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "사진 변경",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 아이디 (비활성)
                FieldLabel("아이디")
                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp)),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color(0xFFE5E7EB),
                        disabledTextColor = Color(0xFF6B7280)
                    )
                )
                Spacer(Modifier.height(16.dp))

                // 닉네임
                FieldLabel("닉네임")
                OutlinedTextField(
                    value = uiState.nickname,
                    onValueChange = { viewModel.updateNickname(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("닉네임을 입력하세요") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,   // 포커스 시 테두리 색
                        unfocusedBorderColor = amber, // 포커스 없을 때 테두리 색
                        cursorColor = MaterialTheme.colorScheme.primary           // 커서 색도 Primary
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 소개글
                FieldLabel("소개글")
                OutlinedTextField(
                    value = uiState.introduction,
                    onValueChange = {
                        if (it.length <= 100) { // 100자 제한
                            viewModel.updateIntroduction(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text(color = Color.LightGray, text = "자신을 소개하는 글을 작성해보세요") },
                    maxLines = 4,
                    supportingText = {
                        Text(
                            text = "${uiState.introduction.length}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.introduction.length > 90) Color.Red else Color.Gray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = amber,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 새로운 비밀번호
                FieldLabel("새로운 비밀번호")
                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = { viewModel.updateNewPassword(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("새로운 비밀번호를 입력하세요") },
                    visualTransformation = if (uiState.newPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleNewPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.newPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (uiState.newPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("새") ||
                                    uiState.passwordValidationError!!.contains("최소 6자") ||
                                    uiState.passwordValidationError!!.contains("달라야")))
                            Color.Red else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("새") ||
                                    uiState.passwordValidationError!!.contains("최소 6자") ||
                                    uiState.passwordValidationError!!.contains("달라야")))
                            Color.Red else amber,
                        errorBorderColor = Color.Red,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    // 오류 상태 표시
                    isError = !uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("새") ||
                                    uiState.passwordValidationError!!.contains("최소 6자") ||
                                    uiState.passwordValidationError!!.contains("달라야"))
                )
                if (!uiState.passwordValidationError.isNullOrEmpty() &&
                    (uiState.passwordValidationError!!.contains("새") ||
                            uiState.passwordValidationError!!.contains("최소 6자") ||
                            uiState.passwordValidationError!!.contains("달라야"))) {
                    Text(
                        text = uiState.passwordValidationError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 새로운 비밀번호 확인
                FieldLabel("새로운 비밀번호 확인")
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("새로운 비밀번호를 다시 입력하세요") },
                    visualTransformation = if (uiState.confirmPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.confirmPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (uiState.confirmPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            uiState.passwordValidationError!!.contains("일치하지"))
                            Color.Red else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            uiState.passwordValidationError!!.contains("일치하지"))
                            Color.Red else amber,
                        errorBorderColor = Color.Red,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    // 오류 상태 표시
                    isError = !uiState.passwordValidationError.isNullOrEmpty() &&
                            uiState.passwordValidationError!!.contains("일치하지")
                )
                if (!uiState.passwordValidationError.isNullOrEmpty() &&
                    uiState.passwordValidationError!!.contains("일치하지")) {
                    Text(
                        text = uiState.passwordValidationError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 현재 비밀번호 확인
                FieldLabel("현재 비밀번호 확인")
                OutlinedTextField(
                    value = uiState.currentPassword,
                    onValueChange = { viewModel.updateCurrentPassword(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("현재 비밀번호를 입력하세요") },
                    visualTransformation = if (uiState.currentPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleCurrentPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.currentPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (uiState.currentPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("현재 비밀번호") ||
                                    uiState.passwordValidationError!!.contains("현재") ||
                                    uiState.passwordValidationError!!.contains("입력해주세요")))
                            Color.Red else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (!uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("현재 비밀번호") ||
                                    uiState.passwordValidationError!!.contains("현재") ||
                                    uiState.passwordValidationError!!.contains("입력해주세요")))
                            Color.Red else amber,
                        errorBorderColor = Color.Red,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    // 오류 상태 표시
                    isError = !uiState.passwordValidationError.isNullOrEmpty() &&
                            (uiState.passwordValidationError!!.contains("현재 비밀번호") ||
                                    uiState.passwordValidationError!!.contains("현재") ||
                                    uiState.passwordValidationError!!.contains("입력해주세요"))
                )
                if (!uiState.passwordValidationError.isNullOrEmpty() &&
                    (uiState.passwordValidationError!!.contains("현재 비밀번호") ||
                            uiState.passwordValidationError!!.contains("현재") ||
                            uiState.passwordValidationError!!.contains("입력해주세요"))) {
                    Text(
                        text = uiState.passwordValidationError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 동네 인증
                FieldLabel("동네 인증")
                OutlinedTextField(
                    value = if (uiState.isLocationVerified)
                        uiState.currentLocation ?: "위치 정보 없음"
                    else "위치 인증을 진행해주세요",
                    onValueChange = {}, // 읽기 전용
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = bg,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isVerifyingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 1.5.dp,
                                    color = PrimaryColor
                                )
                            } else {
                                IconButton(
                                    onClick = { viewModel.requestLocationVerification() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isLocationVerified)
                                            Icons.Outlined.LocationOn // 재인증용 아이콘
                                        else Icons.Filled.LocationOn, // 초기 인증용 아이콘
                                        contentDescription = if (uiState.isLocationVerified)
                                            "위치 재인증" else "위치 인증",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = if (uiState.isLocationVerified)
                            amber
                        else Color(0xFFE5E7EB),
                        disabledTextColor = if (uiState.isLocationVerified)
                            Color.Black
                        else Color(0xFF6B7280),
                        disabledTrailingIconColor = PrimaryColor
                    )
                )


                // 동네 인증 안내 텍스트
                if (!uiState.isLocationVerified) {
                    Text(
                        text = "• GPS를 통해 현재 위치를 확인합니다\n• 동네 인증 시 근처 이웃들과 더 쉽게 소통할 수 있어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                } else {
                    Text(
                        text = "• 우측 위치 아이콘을 눌러 재인증할 수 있습니다\n• 이사하신 경우 새로운 동네로 재인증해주세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = amber,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
                Spacer(Modifier.height(100.dp)) // 하단 버튼 공간 확보
            }
        }

        // 하단 버튼
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .navigationBarsPadding()
                    .imePadding() // 키보드가 올라올 때 버튼도 같이 올라감
            ) {
                Button(
                    onClick = {
                        viewModel.saveProfile {
                            navController.popBackStack()
                        }
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    } else {
                        Text(
                            "변경하기",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}