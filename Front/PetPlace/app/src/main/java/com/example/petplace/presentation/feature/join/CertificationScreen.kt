package com.example.petplace.presentation.feature.join

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import com.iamport.sdk.domain.core.Iamport
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.BuildConfig
import com.example.petplace.R
import com.iamport.sdk.data.sdk.IamPortCertification
import com.iamport.sdk.data.sdk.IamPortResponse
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CertificationScreen(
    navController: NavController,
    viewModel: JoinViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_certification),
            contentDescription = "본인 인증 안내 이미지",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                // 본인인증 로직
                scope.launch {
                    try {
                        isLoading = true

                        // 1. 서버로부터 merchantUid를 발급받는 API 호출
                        val resp = viewModel.prepareCertification()
                        val merchantUid = resp.data.merchantUid
                        val userCode = BuildConfig.IMP_KEY // ⚠️ 여기에 포트원 가맹점 식별코드 입력

                        // 2. Iamport.certification 함수 호출
                        Iamport.certification(
                            userCode = userCode,
                            iamPortCertification = IamPortCertification(
                                merchant_uid = merchantUid,
                                company ="html5_inicis" ,




                                ),

                            resultCallback = { result: IamPortResponse? ->
                                // 3. 인증 결과 처리
                                isLoading = false
                                if (result?.success == true) {
                                    val impUid = result.imp_uid
                                    if (!impUid.isNullOrEmpty()) {
                                        // 4. 인증 성공 시, imp_uid를 서버로 보내 검증
//                                        viewModel.verifyCertification(impUid) 이건 회원가입할때
                                        viewModel.saveImpUid(result.imp_uid!!)
                                        Toast.makeText(context, "본인인증이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("join/main")
                                    }
                                } else {
                                    val msg = result?.error_msg ?: "인증 실패"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    Log.e("Certification", "본인인증 실패: $msg")
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // API 호출 실패 등 예외 처리
                        isLoading = false
                        errorMessage = e.message ?: "인증 준비 중 오류 발생"
                        Log.e("Certification", "오류 발생: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "로딩 중..." else "휴대폰 본인 인증 시작")
        }

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = androidx.compose.ui.graphics.Color.Red)
        }
    }
}