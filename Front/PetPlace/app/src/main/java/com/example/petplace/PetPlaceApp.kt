package com.example.petplace

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.example.petplace.data.remote.LoginApiService
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import com.example.petplace.BuildConfig
import com.iamport.sdk.domain.core.Iamport
import com.kakao.sdk.common.KakaoSdk


@HiltAndroidApp
class PetPlaceApp : Application() {

    companion object {
        private lateinit var instance: PetPlaceApp
        fun getAppContext(): Context = instance.applicationContext
    }

    private val prefs by lazy {
        getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }
    private val gson = Gson()
    override fun onCreate() {
        super.onCreate()
        instance = this

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        Log.d("KakaoKeyCheck", BuildConfig.KAKAO_REST_KEY)
        //본인인증 sdk
//        Iamport.create(this)
//        Iamport.init(this)
    }

    // 토큰 + 유저저 장
    fun saveLoginData(
        accessToken: String,
        refreshToken: String,
        user: LoginApiService.User
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_info", gson.toJson(user))
            .apply()

//        Log.d("userInfo", "saveLoginData:${user.userName}")

    }
    fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    //토큰 가져오기
    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    //유저정보 불러오기
    @SuppressLint("SuspiciousIndentation")
    fun getUserInfo(): LoginApiService.User? {
        val json = prefs.getString("user_info", null) ?: return null
//        Log.d("userInfo", "saveLoginData:${gson.fromJson(json, LoginApiService.User::class.java).userName}")

            return gson.fromJson(json, LoginApiService.User::class.java)
    }

    //로그아웃
    fun clearLoginData() {
        prefs.edit().clear().apply()
    }
}
