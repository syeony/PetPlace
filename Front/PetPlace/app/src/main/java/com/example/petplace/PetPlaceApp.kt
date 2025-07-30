package com.example.petplace

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PetPlaceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this,"89637d56f28078a2a1b91eab431505d5") // AndroidManifest의 키 사용
//        KakaoSdk.init(this, "")
        Log.d("KakaoKeyCheck", BuildConfig.KAKAO_REST_KEY)

    }
}
