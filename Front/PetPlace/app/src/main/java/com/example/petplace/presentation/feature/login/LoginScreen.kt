package com.example.petplace.presentation.feature.login

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petplace.R
import com.example.petplace.data.model.login.KakaoLoginRequest
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundSoft
import com.example.petplace.presentation.common.theme.DividerColor
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.example.petplace.presentation.common.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    val locationPerm = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 최초 컴포지션 시 권한 요청
    LaunchedEffect(Unit) {
        locationPerm.launchMultiplePermissionRequest()
    }

    // 권한 상태에 따라 메시지 출력만 (권한 요청 X)
    if (!locationPerm.allPermissionsGranted) {
        LaunchedEffect(locationPerm.allPermissionsGranted) {
            Toast.makeText(context, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 종료 확인 다이얼로그 상태
    var showExitDialog by remember { mutableStateOf(false) }

    // 뒤로가기 핸들러
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("프로그램 종료") },
            text = { Text("프로그램을 종료하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = { activity.finish() }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.pp_logo),
                contentDescription = "Logo Image",
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
            Text("Pet Place", style = AppTypography.headlineLarge)
            Text("우리동네 펫 커뮤니티", style = AppTypography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("아이디", style = AppTypography.labelLarge) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = BackgroundSoft
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                label = { Text("비밀번호", style = AppTypography.labelLarge) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = BackgroundSoft
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "비밀번호를 잊으셨나요?",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (id.isEmpty()) {
                        Toast.makeText(context, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else if (pw.isEmpty()) {
                        Toast.makeText(context, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.login(id, pw)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("로그인", style = AppTypography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.navigate("join_graph") },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, color = PrimaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("회원가입", color = PrimaryColor, style = AppTypography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(modifier = Modifier.weight(1f), color = DividerColor)

                Text(
                    text = "또는",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = Color.Gray,
                    style = AppTypography.labelLarge
                )

                Divider(modifier = Modifier.weight(1f), color = DividerColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.kakaoLoginAndSendToServer(
                        context = context,
                        onNavigateToJoin = { tempToken ->
                            navController.navigate("kakao_join_check/${tempToken}")
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_login),
                    contentDescription = "카카오 로그인",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }



            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) { append("로그인하면 ") }
                    withStyle(style = SpanStyle(color = Color(0xFFFF9800))) { append("이용약관") }
                    withStyle(style = SpanStyle(color = Color.Gray)) { append(" 및 ") }
                    withStyle(style = SpanStyle(color = Color(0xFFFF9800))) { append("개인정보처리방침") }
                    withStyle(style = SpanStyle(color = Color.Gray)) { append(" 에 동의하는 것으로 간주됩니다.") }
                },
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

    }

    // 로그인 상태에 따른 처리
    LaunchedEffect(loginState) {
        when {
            loginState.isLoading -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("로그인 중...")
                }
            }
            loginState.isSuccess -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("로그인 성공!")
                }
                navController.navigate("nav_feed") {
                    popUpTo("login") { inclusive = true }
                }
            }
            loginState.error != null -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("로그인 실패: ${loginState.error}")
                }
            }
        }
    }
    }
}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFEF9F0)
//@Composable
//fun LoginPreview() {
//    LoginScreen(navController = rememberNavController())
//}
