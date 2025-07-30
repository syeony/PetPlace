plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android") version "2.48"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
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
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
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
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    implementation("com.google.android.material:material:1.11.0")

    // Navigation (Compose용)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase Cloud Messaging (FCM)
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // 위치
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 카메라
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //board 피드에서 필요
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")


}
