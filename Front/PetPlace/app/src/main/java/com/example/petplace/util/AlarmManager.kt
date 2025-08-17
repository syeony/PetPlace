package com.example.petplace.utils

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmsPrefs by lazy {
        context.getSharedPreferences("fcm_alarms", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "AlarmManager"
    }

    fun saveAlarmFromFCM(
        title: String,
        body: String,
        refType: String? = null,
        refId: String? = null,
        chatId: String? = null,
        userId: String? = null
    ) {
        try {
            val alarmId = "alarm_${System.currentTimeMillis()}"
            val message = if (title.isNotEmpty() && body.isNotEmpty() && title != body) {
                "$title\n$body"
            } else {
                body.ifEmpty { title }
            }

            val alarmDataString = "$alarmId|$message|${System.currentTimeMillis()}|false|$refType|$refId|$chatId|$userId"

            alarmsPrefs.edit()
                .putString(alarmId, alarmDataString)
                .apply()

            Log.d(TAG, "Saved FCM alarm: $alarmId")
            Log.d(TAG, "Message: $message")
            Log.d(TAG, "RefType: $refType, RefId: $refId, ChatId: $chatId")

        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM alarm: ${e.message}")
        }
    }

    fun getUnreadCount(): Int {
        return try {
            val allPrefs = alarmsPrefs.all
            var unreadCount = 0

            allPrefs.keys.forEach { key ->
                if (key.startsWith("alarm_")) {
                    val alarmData = alarmsPrefs.getString(key, null)
                    alarmData?.let { data ->
                        val parts = data.split("|")
                        if (parts.size >= 4) {
                            val isRead = parts[3].toBoolean()
                            if (!isRead) {
                                unreadCount++
                            }
                        }
                    }
                }
            }

            unreadCount
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count: ${e.message}")
            0
        }
    }
}