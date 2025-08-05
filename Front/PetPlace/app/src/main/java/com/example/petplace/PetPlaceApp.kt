package com.example.petplace

import android.app.Application
import android.content.Context
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import com.example.petplace.BuildConfig

@HiltAndroidApp
class PetPlaceApp : Application() {

    companion object {
        private lateinit var instance: PetPlaceApp
        fun getAppContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        Log.d("KakaoKeyCheck", BuildConfig.KAKAO_REST_KEY)
    }

    // 🔹 JWT 관리 함수
    fun saveJwtToken(token: String) {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getJwtToken(): String? {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }

    fun clearJwtToken() {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("jwt_token").apply()
    }
}
