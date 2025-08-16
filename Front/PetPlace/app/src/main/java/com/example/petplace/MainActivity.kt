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
import kotlinx.coroutines.delay
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
        Log.e("KeyHash", "해시값 : $keyHash")
        Iamport.init(this)

        setContent {
            PetPlaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // NavController 설정 후 FCM 처리
                    LaunchedEffect(navController) {
                        mainNavController = navController

                        // 짧은 지연 후 FCM 네비게이션 처리
                        delay(200)
                        handleFCMNavigationDirect()
                    }

                    MainScaffold(navController = navController)
                }
            }
        }

        // onCreate에서 FCM Intent 처리
        handleFCMIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("FCM_INTENT", "=== onNewIntent called ===")
        setIntent(intent)

        handleFCMIntent(intent)

        lifecycleScope.launch {
            kotlinx.coroutines.delay(200)
            handleFCMNavigationDirect()
        }
    }

    private fun handleFCMIntent(intent: Intent) {
        Log.d("FCM_INTENT", "=== handleFCMIntent ===")

        // 1. 포그라운드에서 생성된 커스텀 알림인지 확인
        val isFromFCM = intent.getBooleanExtra("FROM_FCM_NOTIFICATION", false)
        Log.d("FCM_INTENT", "Is from FCM notification: $isFromFCM")

        if (isFromFCM) {
            handleCustomFCMIntent(intent)
        } else {
            // 2. 백그라운드에서 Firebase가 자동 생성한 알림인지 확인
            handleBackgroundFCMIntent(intent)
        }

        // 3. 기존 방법들도 유지 (fallback)
        checkRegularIntentExtras(intent)
        checkNotificationData()
        checkBackupFCMData()
    }

    private fun handleCustomFCMIntent(intent: Intent) {
        Log.d("FCM_INTENT", "=== handleCustomFCMIntent (Foreground) ===")

        val extras = intent.extras
        Log.d("FCM_INTENT", "Intent extras keys: ${extras?.keySet()?.joinToString()}")

        var refType: String? = null
        var refId: String? = null
        var fcmType: String? = null
        var chatId: String? = null

        // 모든 extras를 로그로 출력하여 채팅 데이터 확인
        extras?.keySet()?.forEach { key ->
            val value = extras.getString(key)
            Log.d("FCM_INTENT_DETAIL", "Extra: $key = $value")

            when (key) {
                "fcm_refType", "fcm_ref_type" -> refType = value
                "fcm_refId", "fcm_ref_id" -> refId = value
                "fcm_type" -> fcmType = value
                "fcm_chat_id", "fcm_chatId", "fcm_chatRoomId" -> chatId = value
                MyFirebaseMessagingService.EXTRA_REF_TYPE -> refType = value
                MyFirebaseMessagingService.EXTRA_REF_ID -> refId = value
                MyFirebaseMessagingService.EXTRA_FCM_TYPE -> fcmType = value
                MyFirebaseMessagingService.EXTRA_CHAT_ID -> chatId = value
            }
        }

        saveFCMData(refType, refId, fcmType, chatId, "handleCustomFCMIntent")
    }

    private fun handleBackgroundFCMIntent(intent: Intent) {
        Log.d("FCM_INTENT", "=== handleBackgroundFCMIntent (Background) ===")

        val extras = intent.extras
        if (extras != null) {
            Log.d("FCM_INTENT", "Background intent extras keys: ${extras.keySet()?.joinToString()}")

            var refType: String? = null
            var refId: String? = null
            var fcmType: String? = null
            var chatId: String? = null

            // Firebase가 백그라운드에서 자동 생성하는 키들 확인
            extras.keySet()?.forEach { key ->
                val value = extras.getString(key) ?: extras.get(key)?.toString()
                Log.d("FCM_INTENT_DETAIL", "Background Extra: $key = $value")

                when (key) {
                    // Firebase 기본 키들
                    "google.message_id", "google.sent_time", "google.ttl" -> {
                        // 시스템 키들은 무시
                    }
                    // 커스텀 데이터 키들 (data payload에서 온 것들)
                    "refType", "ref_type", "type" -> refType = value
                    "refId", "ref_id", "id" -> refId = value
                    "fcm_type", "fcmType" -> fcmType = value
                    "chatId", "chat_id", "chatRoomId", "fcm_chat_id" -> chatId = value
                    // 다른 가능한 키 패턴들
                    else -> {
                        if (key.contains("ref", ignoreCase = true) && key.contains("type", ignoreCase = true)) {
                            refType = value
                        } else if (key.contains("ref", ignoreCase = true) && key.contains("id", ignoreCase = true)) {
                            refId = value
                        } else if (key.contains("chat", ignoreCase = true) && key.contains("id", ignoreCase = true)) {
                            chatId = value
                        } else if (key.contains("type", ignoreCase = true)) {
                            fcmType = value ?: refType
                        }
                    }
                }
            }

            // 추가 확인: Bundle에서 직접 읽기
            extras.keySet()?.forEach { key ->
                try {
                    val bundle = extras.getBundle(key)
                    if (bundle != null) {
                        Log.d("FCM_INTENT", "Found bundle in key: $key")
                        bundle.keySet()?.forEach { bundleKey ->
                            val bundleValue = bundle.getString(bundleKey)
                            Log.d("FCM_INTENT_DETAIL", "Bundle Extra: $bundleKey = $bundleValue")

                            when (bundleKey) {
                                "refType", "ref_type", "type" -> refType = refType ?: bundleValue
                                "refId", "ref_id", "id" -> refId = refId ?: bundleValue
                                "chatId", "chat_id", "chatRoomId" -> chatId = chatId ?: bundleValue
                                "fcm_type", "fcmType" -> fcmType = fcmType ?: bundleValue
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Bundle이 아닌 경우 무시
                }
            }

            if (refType != null || refId != null || fcmType != null || chatId != null) {
                Log.d("FCM_INTENT", "Found background FCM data")
                saveFCMData(refType, refId, fcmType, chatId, "handleBackgroundFCMIntent")
            }
        }
    }

    private fun saveFCMData(refType: String?, refId: String?, fcmType: String?, chatId: String?, source: String) {
        Log.d("FCM_INTENT", "=== saveFCMData from $source ===")
        Log.d("FCM_INTENT", "refType: $refType")
        Log.d("FCM_INTENT", "refId: $refId")
        Log.d("FCM_INTENT", "fcmType: $fcmType")
        Log.d("FCM_INTENT", "chatId: $chatId")

        // 채팅 관련 검증
        val isChatRelated = refType?.equals("CHAT", ignoreCase = true) == true ||
                fcmType?.equals("chat", ignoreCase = true) == true ||
                chatId != null
        Log.d("FCM_INTENT", "Is chat related: $isChatRelated")

        // SharedPreferences에 저장하기 전에 로그 출력
        Log.d("FCM_INTENT", "=== SAVING TO SHARED PREFERENCES ===")
        fcmDataPrefs.edit().apply {
            refType?.let {
                putString("fcm_ref_type", it)
                Log.d("FCM_INTENT", "Saved fcm_ref_type: $it")
            }
            refId?.let {
                putString("fcm_ref_id", it)
                Log.d("FCM_INTENT", "Saved fcm_ref_id: $it")
            }
            fcmType?.let {
                putString("fcm_type", it)
                Log.d("FCM_INTENT", "Saved fcm_type: $it")
            }
            chatId?.let {
                putString("fcm_chat_id", it)
                Log.d("FCM_INTENT", "Saved fcm_chat_id: $it")
            }
            putBoolean("fcm_pending", true)
            putLong("fcm_timestamp", System.currentTimeMillis())
            Log.d("FCM_INTENT", "Set fcm_pending to true")
            apply()
        }

        // 저장된 데이터 확인
        Log.d("FCM_INTENT", "=== VERIFICATION OF SAVED DATA ===")
        Log.d("FCM_INTENT", "fcm_ref_type: ${fcmDataPrefs.getString("fcm_ref_type", "null")}")
        Log.d("FCM_INTENT", "fcm_ref_id: ${fcmDataPrefs.getString("fcm_ref_id", "null")}")
        Log.d("FCM_INTENT", "fcm_type: ${fcmDataPrefs.getString("fcm_type", "null")}")
        Log.d("FCM_INTENT", "fcm_chat_id: ${fcmDataPrefs.getString("fcm_chat_id", "null")}")
        Log.d("FCM_INTENT", "fcm_pending: ${fcmDataPrefs.getBoolean("fcm_pending", false)}")
    }

    private fun checkRegularIntentExtras(intent: Intent) {
        val fcmType = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_FCM_TYPE)
        val refType = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_REF_TYPE)
        val refId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_REF_ID)
        val chatId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_CHAT_ID)

        Log.d("FCM_INTENT", "Regular extras - Type: $fcmType, RefType: $refType, RefId: $refId, ChatId: $chatId")

        if (fcmType != null || refType != null || refId != null || chatId != null) {
            fcmDataPrefs.edit().apply {
                fcmType?.let { putString("fcm_type", it) }
                refType?.let { putString("fcm_ref_type", it) }
                refId?.let { putString("fcm_ref_id", it) }
                chatId?.let { putString("fcm_chat_id", it) }
                putBoolean("fcm_pending", true)
                apply()
            }

            lifecycleScope.launch {
                kotlinx.coroutines.delay(300)
                handleFCMNavigationDirect()
            }
        }
    }

    private fun checkNotificationData() {
        val notificationPrefs = getSharedPreferences("fcm_notification_data", Context.MODE_PRIVATE)
        val hasPendingNotification = notificationPrefs.getBoolean("has_pending_notification", false)

        Log.d("FCM_INTENT", "Checking notification data - hasPending: $hasPendingNotification")

        if (hasPendingNotification) {
            val timestamp = notificationPrefs.getLong("notification_timestamp", 0)
            val currentTime = System.currentTimeMillis()

            // 5분 이내의 알림만 처리
            if (currentTime - timestamp < 5 * 60 * 1000) {
                val refType = notificationPrefs.getString("refType", null)
                val refId = notificationPrefs.getString("refId", null)
                val type = notificationPrefs.getString("type", null)

                Log.d("FCM_INTENT", "Found notification data - refType: $refType, refId: $refId, type: $type")

                fcmDataPrefs.edit().apply {
                    refType?.let {
                        putString("fcm_ref_type", it)
                        putString("fcm_type", it.lowercase())
                    }
                    refId?.let { putString("fcm_ref_id", it) }
                    type?.let { putString("fcm_type", it) }
                    putBoolean("fcm_pending", true)
                    apply()
                }

                // 사용된 알림 데이터 정리
                notificationPrefs.edit().clear().apply()

                lifecycleScope.launch {
                    kotlinx.coroutines.delay(300)
                    handleFCMNavigationDirect()
                }
            } else {
                // 오래된 데이터는 정리
                notificationPrefs.edit().clear().apply()
            }
        }
    }

    private fun checkBackupFCMData() {
        val hasBackupData = fcmDataPrefs.getBoolean("has_fcm_data", false)

        if (hasBackupData) {
            Log.d("FCM_BACKUP", "Found backup FCM data")

            val backupRefType = fcmDataPrefs.getString("backup_refType", null)
                ?: fcmDataPrefs.getString("backup_ref_type", null)
            val backupRefId = fcmDataPrefs.getString("backup_refId", null)
                ?: fcmDataPrefs.getString("backup_ref_id", null)
            val backupType = fcmDataPrefs.getString("backup_type", null)
            val backupChatId = fcmDataPrefs.getString("backup_chat_id", null)
                ?: fcmDataPrefs.getString("backup_chatId", null)
            val backupUserId = fcmDataPrefs.getString("backup_user_id", null)
                ?: fcmDataPrefs.getString("backup_userId", null)

            Log.d("FCM_BACKUP", "Backup - RefType: $backupRefType, RefId: $backupRefId, Type: $backupType")

            fcmDataPrefs.edit().apply {
                backupRefType?.let {
                    putString("fcm_ref_type", it)
                    putString("fcm_type", it.lowercase())
                }
                backupRefId?.let { putString("fcm_ref_id", it) }
                backupType?.let { putString("fcm_type", it) }
                backupChatId?.let { putString("fcm_chat_id", it) }
                backupUserId?.let { putString("fcm_user_id", it) }
                putBoolean("fcm_pending", true)

                // 백업 데이터 정리
                remove("has_fcm_data")
                // 백업 키들 개별적으로 제거
                remove("backup_refType")
                remove("backup_ref_type")
                remove("backup_refId")
                remove("backup_ref_id")
                remove("backup_type")
                remove("backup_chat_id")
                remove("backup_chatId")
                remove("backup_user_id")
                remove("backup_userId")

                apply()
            }

            // 네비게이션 처리
            lifecycleScope.launch {
                kotlinx.coroutines.delay(300)
                handleFCMNavigationDirect()
            }
        } else {
            Log.d("FCM_BACKUP", "No backup FCM data found")
        }
    }

    // ... (나머지 메서드들은 기존과 동일)
    private fun handleFCMNavigationDirect() {
        Log.d("FCM_NAV", "=== handleFCMNavigationDirect ===")

        mainNavController?.let { navController ->
            val hasPendingFcm = fcmDataPrefs.getBoolean("fcm_pending", false)

            Log.d("FCM_NAV", "Has pending FCM: $hasPendingFcm")

            if (hasPendingFcm) {
                val refType = fcmDataPrefs.getString("fcm_ref_type", null)
                val refId = fcmDataPrefs.getString("fcm_ref_id", null)
                val chatId = fcmDataPrefs.getString("fcm_chat_id", null)
                val userId = fcmDataPrefs.getString("fcm_user_id", null)
                val timestamp = fcmDataPrefs.getLong("fcm_timestamp", 0)

                Log.d("FCM_NAV", "FCM Data - refType: $refType, refId: $refId, chatId: $chatId")

                // 5분 이내의 FCM만 처리 (너무 오래된 데이터 방지)
                val currentTime = System.currentTimeMillis()
                if (timestamp > 0 && currentTime - timestamp > 5 * 60 * 1000) {
                    Log.d("FCM_NAV", "FCM data is too old, ignoring")
                    fcmDataPrefs.edit().clear().apply()
                    return
                }

                // FCM 데이터 클리어 (먼저 클리어)
                fcmDataPrefs.edit().clear().apply()

                // 현재 화면 확인
                val currentRoute = navController.currentDestination?.route
                Log.d("FCM_NAV", "Current route: $currentRoute")

                // splash/login 화면에서는 지연 처리
                if (currentRoute == "splash" || currentRoute == "login") {
                    Log.d("FCM_NAV", "Waiting for app initialization...")
                    lifecycleScope.launch {
                        delay(1500) // 앱 초기화 대기
                        performNavigation(navController, refType, refId, chatId, userId)
                    }
                } else {
                    // 즉시 네비게이션
                    performNavigation(navController, refType, refId, chatId, userId)
                }
            } else {
                Log.d("FCM_NAV", "No pending FCM data")
            }
        } ?: run {
            Log.d("FCM_NAV", "NavController is null, retrying...")
            // NavController가 null이면 잠시 후 재시도
            lifecycleScope.launch {
                delay(500)
                handleFCMNavigationDirect()
            }
        }
    }

    private fun performNavigation(
        navController: NavHostController,
        refType: String?,
        refId: String?,
        chatId: String?,
        userId: String?
    ) {
        Log.d("FCM_NAV", "=== performNavigation ===")
        Log.d("FCM_NAV", "refType: $refType, refId: $refId, chatId: $chatId")

        try {
            when (refType?.uppercase()) {
                "CHAT" -> {
                    val idToUse = refId ?: chatId
                    idToUse?.toLongOrNull()?.let { id ->
                        Log.d("FCM_NAV", "Navigating to chat: $id")
                        navController.navigate("chatDetail/$id") {
                            launchSingleTop = true
                        }
                    } ?: run {
                        Log.d("FCM_NAV", "No chat ID, going to chat list")
                        navController.navigate("chat")
                    }
                }
                "SIGHTING" -> {
                    refId?.toLongOrNull()?.let { id ->
                        Log.d("FCM_NAV", "Navigating to missing report: $id")
                        navController.navigate("missingReportDetail/$id") {
                            launchSingleTop = true
                        }
                    } ?: run {
                        Log.d("FCM_NAV", "No missing report ID, going to missing list")
                        navController.navigate("missing_list")
                    }
                }
                "sighting" -> {
                    refId?.toLongOrNull()?.let { id ->
                        Log.d("FCM_NAV", "Navigating to missing report (from fcmType): $id")
                        try {
                            navController.navigate("missingReportDetail/$id") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("FCM_NAV", "Error navigating to missing report: ${e.message}")
                            navController.navigate("missing_list")
                        }
                    } ?: run {
                        Log.d("FCM_NAV", "No missing report ID found, going to missing list")
                        navController.navigate("missing_list")
                    }
                }
                "FEED" -> {
                    refId?.toLongOrNull()?.let { id ->
                        Log.d("FCM_NAV", "Navigating to feed: $id")
                        navController.navigate("feedDetail/$id") {
                            launchSingleTop = true
                        }
                    } ?: run {
                        Log.d("FCM_NAV", "No feed ID, going to feed list")
                        navController.navigate("feed")
                    }
                }
                else -> {
                    // chatId가 있으면 채팅으로 간주
                    if (!chatId.isNullOrEmpty()) {
                        chatId.toLongOrNull()?.let { id ->
                            Log.d("FCM_NAV", "Navigating to chat (by chatId): $id")
                            navController.navigate("chatDetail/$id") {
                                launchSingleTop = true
                            }
                        } ?: navController.navigate("chat")
                    } else {
                        Log.d("FCM_NAV", "Unknown type, going to feed")
                        navController.navigate("feed")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FCM_NAV", "Navigation error: ${e.message}")
            try {
                navController.navigate("feed")
            } catch (e2: Exception) {
                Log.e("FCM_NAV", "Fallback navigation failed: ${e2.message}")
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