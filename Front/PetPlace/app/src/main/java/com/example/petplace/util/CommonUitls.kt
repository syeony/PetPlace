package com.example.petplace.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationServices
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
    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit
    ) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    onLocationReceived(it.latitude, it.longitude)
                }
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