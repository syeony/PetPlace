package com.example.petplace

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.petplace.presentation.common.navigation.MainScaffold
import com.example.petplace.presentation.common.theme.PetPlaceTheme
import com.example.petplace.service.MyFirebaseMessagingService
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.iamport.sdk.domain.core.Iamport
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val fcmDataPrefs by lazy {
        getSharedPreferences("fcm_data", Context.MODE_PRIVATE)
    }

    private var mainNavController: NavHostController? = null

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 알림 채널 생성
        createNotificationChannel()
        FirebaseApp.initializeApp(this)

        // Android 13+ 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val keyHash = Utility.getKeyHash(this)
        Log.e("KeyHash", "해쉬값 : $keyHash")
        Iamport.init(this)

        setContent {
            PetPlaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // NavController를 저장
                    LaunchedEffect(navController) {
                        mainNavController = navController
                    }

                    MainScaffold(navController = navController)
                }
            }
        }

        // onCreate에서 FCM 처리
        handleFCMIntent(intent)


    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFCMIntent(intent)
        // onNewIntent에서도 즉시 네비게이션 처리
        lifecycleScope.launch {
            kotlinx.coroutines.delay(100) // 약간의 딜레이 후
            handleFCMNavigationDirect()
        }
    }

    private fun handleFCMIntent(intent: Intent) {
        val fcmType = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_FCM_TYPE)
        val refType = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_REF_TYPE)
        val refId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_REF_ID)
        val chatId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_CHAT_ID)
        val userId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_USER_ID)
        val notificationId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_NOTIFICATION_ID)

        Log.d("FCM_INTENT", "Type: $fcmType, RefType: $refType, RefId: $refId, ChatId: $chatId, UserId: $userId, NotificationId: $notificationId")

        if (fcmType != null || refType != null) {
            fcmDataPrefs.edit().apply {
                fcmType?.let { putString("fcm_type", it) }
                refType?.let { putString("fcm_ref_type", it) }
                refId?.let { putString("fcm_ref_id", it) }
                chatId?.let { putString("fcm_chat_id", it) }
                userId?.let { putString("fcm_user_id", it) }
                notificationId?.let { putString("fcm_notification_id", it) }
                putBoolean("fcm_pending", true)
                apply()
            }

            // MainActivity에서 직접 네비게이션 처리
            lifecycleScope.launch {
                kotlinx.coroutines.delay(500) // UI가 준비될 때까지 대기
                handleFCMNavigationDirect()
            }
        }
    }

    private fun handleFCMNavigationDirect() {
        mainNavController?.let { navController ->
            val hasPendingFcm = fcmDataPrefs.getBoolean("fcm_pending", false)

            if (hasPendingFcm) {
                val refType = fcmDataPrefs.getString("fcm_ref_type", null)
                val refId = fcmDataPrefs.getString("fcm_ref_id", null)

                Log.d("FCM_NAV_DIRECT", "Direct navigation - RefType: $refType, RefId: $refId")

                // FCM 데이터 클리어
                fcmDataPrefs.edit().clear().apply()

                when (refType?.uppercase()) {
                    "CHAT" -> {
                        refId?.toLongOrNull()?.let { id ->
                            Log.d("FCM_NAV_DIRECT", "Navigating to chat: $id")
                            navController.navigate("chatDetail/$id")
                        }
                    }
                    "FEED" -> {
                        refId?.toLongOrNull()?.let { id ->
                            Log.d("FCM_NAV_DIRECT", "Navigating to chat: $id")
                            navController.navigate("feedDetail/$id")
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "default",
                "기본 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }
}


