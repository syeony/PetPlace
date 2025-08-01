package com.example.petplace.di

import android.util.Log
import com.example.petplace.data.remote.KakaoApiService
import com.example.petplace.data.remote.ServerApiService
import com.example.petplace.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
    private const val SERVER_BASE_URL = "https://api.ourserver.com/"
    private const val KAKAO_API_KEY = BuildConfig.KAKAO_REST_KEY // 실제 키로 교체

    // 1) 로깅 인터셉터
    private fun loggingInterceptor() =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // 2) Kakao 전용 OkHttpClient
    @Provides
    @Singleton
    @Named("KakaoClient")
    fun provideKakaoOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .addInterceptor(Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_KEY}")
                    .build()
                Log.d("KakaoHeader", "Authorization → ${req.header("Authorization")}")
                chain.proceed(req)
            })
            .build()

    // 3) Server 전용 OkHttpClient
    @Provides
    @Singleton
    @Named("ServerClient")
    fun provideServerOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()

    // 4) Kakao Retrofit (Named 으로 구분)
    @Provides
    @Singleton
    @Named("Kakao")
    fun provideKakaoRetrofit(
        @Named("KakaoClient") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // 5) Server Retrofit
    @Provides
    @Singleton
    @Named("Server")
    fun provideServerRetrofit(
        @Named("ServerClient") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // 6) API 서비스 제공
    @Provides
    @Singleton
    fun provideKakaoApi(
        @Named("Kakao") retrofit: Retrofit
    ): KakaoApiService = retrofit.create(KakaoApiService::class.java)

    @Provides
    @Singleton
    fun provideServerApi(
        @Named("Server") retrofit: Retrofit
    ): ServerApiService = retrofit.create(ServerApiService::class.java)
}
