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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "fcm_notification_channel"
        private const val NOTIFICATION_ID = 1001

        // FCM 데이터를 전달하기 위한 키들
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

        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
        }

        // 앱이 포그라운드에 있을 때도 알림 표시
        if (isAppInForeground()) {
            Log.d(TAG, "App is in foreground - showing notification")
        } else {
            Log.d(TAG, "App is in background")
        }

        // 알림 항상 표시 (포그라운드/백그라운드 관계없이)
        showNotification(
            remoteMessage.notification?.title ?: "PetPlace",
            remoteMessage.notification?.body ?: "새로운 알림이 있습니다.",
            remoteMessage.data
        )
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

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        Log.d(TAG, "=== Showing Notification ===")
        Log.d(TAG, "Title: $title, Body: $body")
        Log.d(TAG, "Data for intent: $data")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // refType을 기본 FCM 타입으로 사용
            data["refType"]?.let {
                putExtra(EXTRA_FCM_TYPE, it.lowercase()) // CHAT -> chat로 변환
                putExtra(EXTRA_REF_TYPE, it)
                Log.d(TAG, "Added FCM_TYPE: ${it.lowercase()}, REF_TYPE: $it")
            }

            // refId를 채팅방 ID로 사용 (refType이 CHAT인 경우)
            data["refId"]?.let { refId ->
                putExtra(EXTRA_REF_ID, refId)
                Log.d(TAG, "Added REF_ID: $refId")

                // refType이 CHAT이면 refId를 chatId로도 저장
                if (data["refType"]?.equals("CHAT", ignoreCase = true) == true) {
                    putExtra(EXTRA_CHAT_ID, refId)
                    Log.d(TAG, "Added CHAT_ID from REF_ID: $refId")
                }
            }

            // FCM 데이터를 Intent에 담기
            data["type"]?.let {
                putExtra(EXTRA_FCM_TYPE, it)
                Log.d(TAG, "Added FCM_TYPE: $it")
            }
            data["chat_id"]?.let {
                putExtra(EXTRA_CHAT_ID, it)
                Log.d(TAG, "Added CHAT_ID: $it")
            }
            data["user_id"]?.let {
                putExtra(EXTRA_USER_ID, it)
                Log.d(TAG, "Added USER_ID: $it")
            }
            data["notification_id"]?.let {
                putExtra(EXTRA_NOTIFICATION_ID, it)
                Log.d(TAG, "Added NOTIFICATION_ID: $it")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.pp_logo) // 임시 아이콘
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        Log.d(TAG, "Notification built and ready to show")
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun handleDataOnlyMessage(data: Map<String, String>) {
        // 데이터만 있는 silent push 처리
        when (data["action"]) {
            "refresh_chat" -> {
                // 채팅 데이터 새로고침 등
            }
            "update_badge" -> {
                // 배지 업데이트
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FCM 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "FCM을 통한 푸시 알림"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}