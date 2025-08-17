package com.example.petplace.service

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.petplace.MainActivity
import com.example.petplace.R
import com.example.petplace.utils.AlarmManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var alarmManager: AlarmManager

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "fcm_notification_channel"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_FCM_TYPE = "fcm_type"
        const val EXTRA_REF_TYPE = "fcm_ref_type"
        const val EXTRA_REF_ID = "fcm_ref_id"
        const val EXTRA_CHAT_ID = "fcm_chat_id"
        const val EXTRA_USER_ID = "fcm_user_id"
        const val EXTRA_NOTIFICATION_ID = "fcm_notification_id"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "=== FCM Message Received ===")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Data payload: ${remoteMessage.data}")
        Log.d(TAG, "Notification payload: ${remoteMessage.notification}")

        // 앱 상태 확인
        val isAppInForeground = isAppInForeground()
        Log.d(TAG, "App is in foreground: $isAppInForeground")

        // 포그라운드에서만 커스텀 알림 표시
        // 백그라운드에서는 Firebase가 자동으로 처리하도록 하되,
        // 데이터를 백업 저장만 해둠
        if (isAppInForeground) {
            val title = remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: remoteMessage.data["notification_title"]
                ?: "PetPlace"

            val body = remoteMessage.notification?.body
                ?: remoteMessage.data["body"]
                ?: remoteMessage.data["notification_body"]
                ?: "새로운 알림이 있습니다."

            Log.d(TAG, "Creating foreground notification - Title: $title, Body: $body")
            showCustomNotification(title, body, remoteMessage.data)
        } else {
            // 백그라운드일 때는 데이터만 백업 저장
            Log.d(TAG, "App in background - saving data for later navigation")
            saveBackgroundFCMData(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")
        // TODO: 서버에 새 토큰 전송
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses

        return runningAppProcesses?.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    it.processName == packageName
        } ?: false
    }

    private fun saveBackgroundFCMData(data: Map<String, String>) {
        Log.d(TAG, "=== Saving Background FCM Data ===")
        Log.d(TAG, "FCM Data: $data")

        // 데이터 추출 및 정규화
        val refType = data["refType"] ?: data["ref_type"] ?: data["type"]
        val refId = data["refId"] ?: data["ref_id"] ?: data["id"]
        val chatId = data["chatId"] ?: data["chat_id"] ?: data["chatRoomId"]
        val userId = data["userId"] ?: data["user_id"]

        val title = data["title"] ?: data["notification_title"] ?: "PetPlace"
        val body = data["body"] ?: data["notification_body"] ?: "새로운 알림이 있습니다."

        alarmManager.saveAlarmFromFCM(
            title = title,
            body = body,
            refType = refType,
            refId = refId,
            chatId = chatId,
            userId = userId
        )


        Log.d(TAG, "Normalized Background Data - refType: $refType, refId: $refId, chatId: $chatId, userId: $userId")

        // FCM 네비게이션 데이터 저장
        val fcmDataPrefs = getSharedPreferences("fcm_data", Context.MODE_PRIVATE)
        fcmDataPrefs.edit().apply {
            clear() // 기존 데이터 클리어

            refType?.let { type ->
                putString("fcm_ref_type", type)
                putString("fcm_type", type.lowercase())
                Log.d(TAG, "Saved background fcm_ref_type: $type")
            }

            refId?.let { id ->
                putString("fcm_ref_id", id)
                Log.d(TAG, "Saved background fcm_ref_id: $id")

                // 채팅 타입인 경우 chatId로도 저장
                if (refType?.equals("CHAT", ignoreCase = true) == true) {
                    putString("fcm_chat_id", id)
                    Log.d(TAG, "Saved as background fcm_chat_id: $id")
                }
            }

            chatId?.let { id ->
                putString("fcm_chat_id", id)
                Log.d(TAG, "Saved background fcm_chat_id: $id")
                // chatId가 있으면 자동으로 CHAT 타입으로 설정
                if (refType.isNullOrEmpty()) {
                    putString("fcm_ref_type", "CHAT")
                    putString("fcm_type", "chat")
                    Log.d(TAG, "Auto-set as CHAT type due to background chatId presence")
                }
            }

            userId?.let { id ->
                putString("fcm_user_id", id)
                Log.d(TAG, "Saved background fcm_user_id: $id")
            }

            putBoolean("fcm_pending", true)
            putLong("fcm_timestamp", System.currentTimeMillis())
            apply()
        }

        Log.d(TAG, "=== Background FCM Data Save Complete ===")
    }

    private fun showCustomNotification(title: String, body: String, data: Map<String, String>) {
        Log.d(TAG, "=== Showing Custom Notification ===")
        Log.d(TAG, "Title: $title, Body: $body")
        Log.d(TAG, "FCM Data: $data")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        // 데이터 추출 및 정규화
        val refType = data["refType"] ?: data["ref_type"] ?: data["type"]
        val refId = data["refId"] ?: data["ref_id"] ?: data["id"]
        val chatId = data["chatId"] ?: data["chat_id"] ?: data["chatRoomId"]
        val userId = data["userId"] ?: data["user_id"]

        Log.d(TAG, "=== Normalized FCM Data ===")
        Log.d(TAG, "refType: $refType")
        Log.d(TAG, "refId: $refId")
        Log.d(TAG, "chatId: $chatId")
        Log.d(TAG, "userId: $userId")

        // SharedPreferences에 백업 저장
        saveToSharedPreferences(refType, refId, chatId, userId, data)

        // 알림을 AlarmManager에 저장 (추가)
        alarmManager.saveAlarmFromFCM(
            title = title,
            body = body,
            refType = refType,
            refId = refId,
            chatId = chatId,
            userId = userId
        )

        // Intent 생성
        val intent = createNotificationIntent(refType, refId, chatId, userId, data)

        // PendingIntent 생성
        val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.pp_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setTicker(body)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        Log.d(TAG, "Showing notification with ID: $notificationId, requestCode: $requestCode")

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun saveToSharedPreferences(
        refType: String?,
        refId: String?,
        chatId: String?,
        userId: String?,
        originalData: Map<String, String>
    ) {
        Log.d(TAG, "=== Saving to SharedPreferences ===")

        // 일반 알림 데이터 저장
        val prefs = getSharedPreferences("fcm_notification_data", Context.MODE_PRIVATE)
        prefs.edit().apply {
            clear()
            originalData.forEach { (key, value) ->
                putString(key, value)
                Log.d(TAG, "Saved to notification prefs: $key = $value")
            }
            putLong("notification_timestamp", System.currentTimeMillis())
            putBoolean("has_pending_notification", true)
            apply()
        }

        // FCM 네비게이션 데이터 저장
        val fcmDataPrefs = getSharedPreferences("fcm_data", Context.MODE_PRIVATE)
        fcmDataPrefs.edit().apply {
            clear() // 기존 데이터 클리어

            refType?.let { type ->
                putString("fcm_ref_type", type)
                putString("fcm_type", type.lowercase())
                Log.d(TAG, "Saved fcm_ref_type: $type")
            }

            refId?.let { id ->
                putString("fcm_ref_id", id)
                Log.d(TAG, "Saved fcm_ref_id: $id")

                // 채팅 타입인 경우 chatId로도 저장
                if (refType?.equals("CHAT", ignoreCase = true) == true) {
                    putString("fcm_chat_id", id)
                    Log.d(TAG, "Saved as fcm_chat_id: $id")
                }
            }

            chatId?.let { id ->
                putString("fcm_chat_id", id)
                Log.d(TAG, "Saved fcm_chat_id: $id")
                // chatId가 있으면 자동으로 CHAT 타입으로 설정
                if (refType.isNullOrEmpty()) {
                    putString("fcm_ref_type", "CHAT")
                    putString("fcm_type", "chat")
                    Log.d(TAG, "Auto-set as CHAT type due to chatId presence")
                }
            }

            userId?.let { id ->
                putString("fcm_user_id", id)
                Log.d(TAG, "Saved fcm_user_id: $id")
            }

            putBoolean("fcm_pending", true)
            putLong("fcm_timestamp", System.currentTimeMillis())
            apply()
        }

        Log.d(TAG, "=== SharedPreferences Save Complete ===")
    }

    private fun createNotificationIntent(
        refType: String?,
        refId: String?,
        chatId: String?,
        userId: String?,
        originalData: Map<String, String>
    ): Intent {
        return Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

            // FCM 식별자 추가
            putExtra("FROM_FCM_NOTIFICATION", true)
            putExtra("FCM_TIMESTAMP", System.currentTimeMillis())

            Log.d(TAG, "=== Creating Intent ===")

            // 정규화된 데이터 추가
            refType?.let {
                putExtra(EXTRA_REF_TYPE, it)
                putExtra(EXTRA_FCM_TYPE, it.lowercase())
                Log.d(TAG, "Intent refType: $it")
            }

            refId?.let {
                putExtra(EXTRA_REF_ID, it)
                Log.d(TAG, "Intent refId: $it")
            }

            chatId?.let {
                putExtra(EXTRA_CHAT_ID, it)
                Log.d(TAG, "Intent chatId: $it")
            }

            userId?.let {
                putExtra(EXTRA_USER_ID, it)
                Log.d(TAG, "Intent userId: $it")
            }

            // 원본 데이터도 모두 추가
            originalData.forEach { (key, value) ->
                putExtra("fcm_$key", value)
                Log.d(TAG, "Intent extra: fcm_$key = $value")
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PetPlace 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "PetPlace 앱의 푸시 알림"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}