package com.example.petplace.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

object CommonUtils {
    /**
     * 전역에서 사용 가능한 토스트 함수
     *
     * @param context  화면 Context
     * @param message  띄울 메시지
     * @param duration Toast 지속 시간 (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    fun makeToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }
    /**
     * 마지막으로 알려진 위치를 받아오는 전역 함수
     *
     * @param context      화면 Context
     * @param onLocationReceived 위도·경도를 받을 콜백
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(location?.let { it.latitude to it.longitude }, null)
                }
                .addOnFailureListener {
                    cont.resume(null, null)
                }
        }

    suspend fun getFcmToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCM", "Device FCM Token: $token")
            token
        } catch (e: Exception) {
            Log.e("FCM", "Failed to get token", e)
            null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getXY(context: Context): Pair<Double, Double>? {
        val client = LocationServices
            .getFusedLocationProviderClient(context)
        val location = client.lastLocation.await()
        return location?.let { it.latitude to it.longitude }
    }
}