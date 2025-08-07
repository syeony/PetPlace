package com.example.petplace.presentation.feature.login

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
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

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
                    // 1) 콜백 정의
                    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                        when {
                            error != null -> {
                                Toast.makeText(
                                    activity,
                                    "카카오 로그인 실패: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            token != null -> {
                                // 2) 프로필 정보 조회
                                UserApiClient.instance.me { user, meError ->
                                    when {
                                        meError != null -> {
                                            Toast.makeText(
                                                activity,
                                                "사용자 정보 요청 실패: ${meError.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        user != null -> {
                                            // 3) DTO 생성
                                            val request = KakaoLoginRequest(
                                                provider = "KAKAO",
                                                userInfo = KakaoLoginRequest.UserInfo(
                                                    socialId     = user.id.toString(),
                                                    email        = user.kakaoAccount?.email.orEmpty(),
                                                    nickname     = user.kakaoAccount?.profile?.nickname.orEmpty(),
                                                    profileImage = user.kakaoAccount?.profile?.profileImageUrl.orEmpty()
                                                )
                                            )
                                            // 4) 서버 인증 & 분기
                                            coroutineScope.launch {
                                                Log.d("KakaoNav", "서버 로그인 요청: $request")
                                                val success = viewModel.loginWithKakao(request)
                                                Log.d("KakaoNav", "loginWithKakao returned: $success")
                                                if (success) {
                                                    // 기존 사용자: 홈으로
                                                    navController.navigate("nav_feed") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    // 신규 사용자: tempToken 꺼내고 회원가입 체크 화면으로
                                                    val sid = Uri.encode(user.id.toString())
                                                    val tmp = Uri.encode(viewModel.tempToken.value)
                                                    Log.d("tempToken", "LoginScreen:$tmp ")
                                                    Log.d("tempToken", "LoginScreen:kakao_join_check/$sid/$tmp")
                                                    navController.navigate("kakao_join_check/$sid/$tmp") { popUpTo("login") { inclusive = true } }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5) 카카오톡/계정 로그인 UI 호출
                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                        UserApiClient.instance.loginWithKakaoTalk(
                            context = activity,
                            callback = kakaoCallback
                        )
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(
                            context = activity,
                            callback = kakaoCallback
                        )
                    }
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
