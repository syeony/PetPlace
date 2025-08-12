plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android") version "2.48"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    kotlin("kapt")
}

android {
    namespace = "com.example.petplace"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.petplace"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "KAKAO_REST_KEY",
            "\"${project.properties["KAKAO_REST_KEY"]}\""
        )
        buildConfigField(
            "String",
            "KAKAO_NATIVE_KEY",
            "\"${project.properties["KAKAO_NATIVE_KEY"]}\""
        )
        buildConfigField(
            "String",
            "IMP_KEY",
            "\"${project.properties["IMP_KEY"]}\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // Jetpack 기본
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Jetpack Compose (Material3 기준)
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation(libs.common)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.navigation.runtime.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")


    // Navigation (Compose용)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 카카오 SDK
    implementation("com.kakao.sdk:v2-all:2.20.0")
    implementation("com.kakao.maps.open:android:2.12.8")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    debugImplementation(libs.androidx.ui.test.manifest)
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // 위치
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 카메라
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // 이미지 로딩 (Coil)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 이미지 자르기
    implementation("com.github.CanHub:Android-Image-Cropper:4.3.2")
    implementation("com.github.yalantis:ucrop:2.2.8")

    // 권한 요청
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Pager
    implementation("com.google.accompanist:accompanist-pager:0.32.0")

    // Compose foundation
    implementation("androidx.compose.foundation:foundation:1.4.3")

    // Compose BOM (일부 의존성 통일)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.material:material-icons-extended")

    // WebSocket 및 STOMP 클라이언트
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // JSON 처리
    implementation("com.google.code.gson:gson:2.10.1")

    // 네트워크 상태 확인 (선택적)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // RxJava (STOMP 라이브러리에서 사용)
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // 달력 Kizitonwose
    implementation("com.kizitonwose.calendar:compose:2.0.0") // 최신 버전 확인


    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //
    implementation("com.github.iamport:iamport-android:v1.4.8") // 최신 버전으로 사용 권장
//    implementation("com.iamport:iamport-android-sdk:1.5.8")
//      implementation("com.github.portone-io:android-sdk:2.2.0") // <-- 이 부분을 추가하세요
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

}

