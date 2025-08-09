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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.BackgroundColor
import com.example.petplace.presentation.common.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController
) {
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    var newPwVisible by remember { mutableStateOf(false) }
    var confirmPwVisible by remember { mutableStateOf(false) }
    var currentPwVisible by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf(userInfo?.nickname ?: "") }

    val amber = PrimaryColor
    val bg = BackgroundColor

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 이미지 선택 런처
    val launcherGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                profileImageUri = uri
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // 커스텀 상단 바
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "프로필 수정",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        "완료",
                        color = amber
                    )
                }
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
                        Image(
                            painter = if (profileImageUri != null)
                                rememberAsyncImagePainter(
                                    profileImageUri
                                ) else painterResource(R.drawable.ic_mypage),
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
                                .offset(x = (-5).dp, y = (-5).dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    modifier = Modifier
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
                    value = userInfo?.userName ?: "",
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(16.dp))

                // 닉네임
                FieldLabel("닉네임")
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("닉네임을 입력하세요") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 새로운 비밀번호
                FieldLabel("새로운 비밀번호")
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier
                        .fillMaxWidth(),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 새로운 비밀번호 확인
                FieldLabel("새로운 비밀번호 확인")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier
                        .fillMaxWidth(),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 현재 비밀번호 확인
                FieldLabel("현재 비밀번호 확인")
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    modifier = Modifier
                        .fillMaxWidth(),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() } // 키보드 숨김
                    )
                )

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
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = amber),
                    shape = RoundedCornerShape(12.dp)
                ) {
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

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}