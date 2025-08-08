package com.example.petplace.presentation.feature.mypage


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    id: String,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onChangeClick: () -> Unit,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newPwVisible by remember { mutableStateOf(false) }
    var confirmPwVisible by remember { mutableStateOf(false) }
    var currentPwVisible by remember { mutableStateOf(false) }

    val amber = Color(0xFFE2A23A) // 화면의 포인트 색상(버튼/카메라 배지)
    val bg = Color(0xFFFAF5E7)    // 밝은 베이지 톤 배경(스크린샷 유사)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필 수정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = onDone) {
                        Text("완료")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Button(
                    onClick = onChangeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = amber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("변경하기")
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 프로필 이미지 + 카메라 오버레이
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // 데모용 이미지: painterResource 교체 가능
                    Image(
                        painter = painterResource(android.R.drawable.sym_def_app_icon),
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        color = amber,
                        shape = CircleShape,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .offset(x = 6.dp, y = 6.dp) // 가장자리 살짝 밖으로
                            .size(36.dp)
                            .clickable { onChangePhotoClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
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
                value = id,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // 닉네임
            FieldLabel("닉네임")
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("닉네임을 입력하세요") }
            )

            Spacer(Modifier.height(16.dp))

            // 새로운 비밀번호
            FieldLabel("새로운 비밀번호")
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("새로운 비밀번호를 입력하세요") },
                visualTransformation = if (newPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPwVisible = !newPwVisible }) {
                        Icon(
                            imageVector = if (newPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (newPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(16.dp))

            // 새로운 비밀번호 확인
            FieldLabel("새로운 비밀번호 확인")
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("새로운 비밀번호를 다시 입력하세요") },
                visualTransformation = if (confirmPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPwVisible = !confirmPwVisible }) {
                        Icon(
                            imageVector = if (confirmPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(16.dp))

            // 현재 비밀번호 확인
            FieldLabel("현재 비밀번호 확인")
            OutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("현재 비밀번호를 입력하세요") },
                visualTransformation = if (currentPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentPwVisible = !currentPwVisible }) {
                        Icon(
                            imageVector = if (currentPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (currentPwVisible) "비밀번호 숨기기" else "비밀번호 보기"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileEditScreenPreview() {
    var nickname by remember { mutableStateOf("이도형") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }

    MaterialTheme {
        ProfileEditScreen(
            id = "dlehgud1234",
            nickname = nickname,
            onNicknameChange = { nickname = it },
            newPassword = newPassword,
            onNewPasswordChange = { newPassword = it },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = { confirmPassword = it },
            currentPassword = currentPassword,
            onCurrentPasswordChange = { currentPassword = it },
            onBack = {},
            onDone = {},
            onChangeClick = {},
            onChangePhotoClick = {}
        )
    }
}