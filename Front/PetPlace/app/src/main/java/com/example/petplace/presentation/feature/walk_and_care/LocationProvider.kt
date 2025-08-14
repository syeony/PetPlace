package com.example.petplace.presentation.feature.walk_and_care

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationProvider {

    @SuppressLint("MissingPermission") // 권한 체크는 호출하는 쪽에서 보장
    suspend fun getCurrentLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)

        // 1) 빠른 값: lastLocation
        val last = runCatching { client.lastLocation.awaitNullable() }.getOrNull()
        if (last != null) return last

        // 2) 정확한 1회 샷
        val cts = com.google.android.gms.tasks.CancellationTokenSource()
        val loc = runCatching {
            client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).awaitNullable()
        }.getOrNull()
        if (loc != null) return loc

        // 3) 아주 드물게 null일 때: 짧게 업데이트 요청 후 첫 값 사용
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMaxUpdates(1)
            .build()

        return suspendCancellableCoroutine { cont ->
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    client.removeLocationUpdates(this)
                    cont.resume(result.lastLocation)
                }
            }
            client.requestLocationUpdates(request, cb, Looper.getMainLooper())
            cont.invokeOnCancellation { client.removeLocationUpdates(cb) }
        }
    }
}

/* ---- 작은 확장함수 ---- */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitNullable(): T? =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resume(null) }
    }
