package com.example.petplace.presentation.feature.login

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
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petplace.R
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.BackgroundColor
import com.example.petplace.presentation.common.theme.BackgroundSoft
import com.example.petplace.presentation.common.theme.DividerColor
import com.example.petplace.presentation.common.theme.PrimaryColor
import com.example.petplace.presentation.common.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class) //텍스트필드 배경색넣는것
@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
        Text("Pet Place",
            style = AppTypography.headlineLarge)
        Text("우리동네 펫 커뮤니티",
            style = AppTypography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        var id by remember { mutableStateOf("") }
        var pw by remember { mutableStateOf("") }

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
            label = { Text("비밀번호"
                , style = AppTypography.labelLarge) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = BackgroundSoft
            )
        )
        Spacer( modifier = Modifier.height(16.dp))
        Text("비밀번호를 잊으셨나요?",
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("nav_feed") {
                    popUpTo("login") { inclusive = true } // 로그인 화면 뒤로가기 방지
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("로그인",
                style = AppTypography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("join")
            },
            modifier = Modifier.fillMaxWidth(),

            border = BorderStroke(1.dp, color = PrimaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("회원가입" , color = PrimaryColor,
                style = AppTypography.labelLarge)
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
                // 카카오 로그인 처리
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // 이미지 높이에 맞게 조절
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp) // 원래 이미지가 둥글다면 그대로
        ) {
            Image(
                painter = painterResource(id = R.drawable.kakao_login), // 너가 넣은 PNG 파일명
                contentDescription = "카카오 로그인",
                contentScale = ContentScale.Fit, // 그림이 안 잘리게 비율 유지
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append("로그인하면 ")
                }

                withStyle(style = SpanStyle(color = Color(0xFFFF9800))) { // 오렌지
                    append("이용약관")
                }

                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append(" 및 ")
                }

                withStyle(style = SpanStyle(color = Color(0xFFFF9800))) {
                    append("개인정보처리방침")
                }

                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append(" 에 동의하는 것으로 간주됩니다.")
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )



    }
}
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFEF9F0
)
@Composable
fun LoginPreview(){
    LoginScreen(navController = rememberNavController())
}
