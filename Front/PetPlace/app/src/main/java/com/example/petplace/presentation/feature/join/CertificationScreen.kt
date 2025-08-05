package com.example.petplace.presentation.feature.join

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun CertificationScreen(
    navController: NavController,
    viewModel: CertificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val redirectUrl = "https://your-server.com/certification/complete"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url.toString()
                        if (url.startsWith(redirectUrl)) {
                            // imp_uid 추출
                            val uri = Uri.parse(url)
                            val impUid = uri.getQueryParameter("imp_uid")
                            if (!impUid.isNullOrEmpty()) {
                                viewModel.verifyCertification(impUid)
                                Toast.makeText(context, "본인인증 완료!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            return true
                        }
                        return false
                    }
                }

                // 포트원 본인인증 페이지 호출
                loadUrl(
                    "https://api.iamport.kr/certifications/your_merchant_uid" +
                            "?redirect_url=$redirectUrl"
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
