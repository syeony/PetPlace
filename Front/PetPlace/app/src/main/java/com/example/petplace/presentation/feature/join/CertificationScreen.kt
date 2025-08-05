package com.example.petplace.presentation.feature.join

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.petplace.R
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CertificationScreen(
    navController: NavController,
    viewModel: JoinViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val redirectUrl = "petplace://certification" // 커스텀 스킴

    // 서버에서 받아올 인증 URL
    var certificationUrl by remember { mutableStateOf<String?>(null) }
    // 에러 메시지
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 로딩 상태
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (certificationUrl == null) {
        // 1️⃣ 인증 시작 전 UI
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
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        try {
                            // suspend fun 호출
                            val resp = viewModel.prepareCertification()
                            certificationUrl = resp.data.certificationUrl
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "인증 URL 생성 실패"
                        } finally {
                            isLoading = false
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
    } else {
        // 2️⃣ 인증 URL 준비되면 WebView 보여주기
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        @Suppress("OverridingDeprecatedMember")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?) =
                            handleUrl(url ?: "")

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ) = handleUrl(request?.url.toString())

                        private fun handleUrl(url: String): Boolean {
                            Log.d("CertScreen", "Redirect URL: $url")
                            if (url.startsWith(redirectUrl)) {
                                val impUid = Uri.parse(url).getQueryParameter("imp_uid")
                                if (!impUid.isNullOrEmpty()) {
                                    viewModel.verifyCertification(impUid)
                                    Toast.makeText(context, "본인인증 완료!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                                }
                                return true
                            }
                            return false
                        }
                    }

                    Log.d("CertScreen", "WebView 로드 시작: $certificationUrl")
                    loadUrl(certificationUrl!!)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
