package com.example.petplace.presentation.feature.alarm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmItem(
    val id: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val refType: String? = null,
    val refId: String? = null,
    val chatId: String? = null,
    val userId: String? = null
)

@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val alarmsPrefs by lazy {
        context.getSharedPreferences("fcm_alarms", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "AlarmViewModel"
    }

    fun loadAlarms() {
        viewModelScope.launch {
            try {
                val alarmList = mutableListOf<AlarmItem>()

                // SharedPreferences에서 모든 알림 로드
                val allPrefs = alarmsPrefs.all

                allPrefs.keys.forEach { key ->
                    if (key.startsWith("alarm_")) {
                        val alarmData = alarmsPrefs.getString(key, null)
                        alarmData?.let { data ->
                            parseAlarmData(data)?.let { alarm ->
                                alarmList.add(alarm)
                            }
                        }
                    }
                }

                // 시간 순으로 정렬 (최신순)
                val sortedAlarms = alarmList.sortedByDescending { it.timestamp }

                _alarms.value = sortedAlarms
                _unreadCount.value = sortedAlarms.count { !it.isRead }

                Log.d(TAG, "Loaded ${sortedAlarms.size} alarms, ${_unreadCount.value} unread")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading alarms: ${e.message}")
            }
        }
    }

    fun markAsRead(alarmId: String) {
        viewModelScope.launch {
            try {
                val currentAlarms = _alarms.value.toMutableList()
                val alarmIndex = currentAlarms.indexOfFirst { it.id == alarmId }

                if (alarmIndex != -1) {
                    val updatedAlarm = currentAlarms[alarmIndex].copy(isRead = true)
                    currentAlarms[alarmIndex] = updatedAlarm

                    // SharedPreferences 업데이트
                    saveAlarmData(updatedAlarm)

                    _alarms.value = currentAlarms
                    _unreadCount.value = currentAlarms.count { !it.isRead }

                    Log.d(TAG, "Marked alarm as read: $alarmId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking alarm as read: ${e.message}")
            }
        }
    }

    fun addAlarm(
        message: String,
        refType: String? = null,
        refId: String? = null,
        chatId: String? = null,
        userId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val alarmId = "alarm_${System.currentTimeMillis()}"
                val newAlarm = AlarmItem(
                    id = alarmId,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    refType = refType,
                    refId = refId,
                    chatId = chatId,
                    userId = userId
                )

                // SharedPreferences에 저장
                saveAlarmData(newAlarm)

                // 현재 목록에 추가
                val currentAlarms = _alarms.value.toMutableList()
                currentAlarms.add(0, newAlarm) // 최신순으로 맨 앞에 추가

                _alarms.value = currentAlarms
                _unreadCount.value = currentAlarms.count { !it.isRead }

                Log.d(TAG, "Added new alarm: $alarmId")

            } catch (e: Exception) {
                Log.e(TAG, "Error adding alarm: ${e.message}")
            }
        }
    }

    private fun saveAlarmData(alarm: AlarmItem) {
        val alarmDataString = "${alarm.id}|${alarm.message}|${alarm.timestamp}|${alarm.isRead}|${alarm.refType}|${alarm.refId}|${alarm.chatId}|${alarm.userId}"
        alarmsPrefs.edit()
            .putString(alarm.id, alarmDataString)
            .apply()
    }

    private fun parseAlarmData(data: String): AlarmItem? {
        return try {
            val parts = data.split("|")
            if (parts.size >= 4) {
                AlarmItem(
                    id = parts[0],
                    message = parts[1],
                    timestamp = parts[2].toLongOrNull() ?: 0L,
                    isRead = parts[3].toBoolean(),
                    refType = if (parts.size > 4 && parts[4] != "null") parts[4] else null,
                    refId = if (parts.size > 5 && parts[5] != "null") parts[5] else null,
                    chatId = if (parts.size > 6 && parts[6] != "null") parts[6] else null,
                    userId = if (parts.size > 7 && parts[7] != "null") parts[7] else null
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing alarm data: ${e.message}")
            null
        }
    }

    fun clearAllAlarms() {
        viewModelScope.launch {
            try {
                alarmsPrefs.edit().clear().apply()
                _alarms.value = emptyList()
                _unreadCount.value = 0
                Log.d(TAG, "Cleared all alarms")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing alarms: ${e.message}")
            }
        }
    }
}