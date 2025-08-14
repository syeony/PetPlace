package com.example.petplace.di

import android.content.Context
import android.util.Log
import com.example.petplace.BuildConfig
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.CaresApiService
import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.remote.FeedApiService
import com.example.petplace.data.remote.HotelApiService
import com.example.petplace.data.remote.ImageApiService
import com.example.petplace.data.remote.JoinApiService
import com.example.petplace.data.remote.KakaoApiService
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.remote.MissingApiService
import com.example.petplace.data.remote.MyPageApiService
import com.example.petplace.data.remote.PaymentsApiService
import com.example.petplace.data.remote.PetApiService
import com.example.petplace.data.remote.TokenApiService
import com.example.petplace.data.remote.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
//    private const val SERVER_BASE_URL = "http://43.201.108.195:8081/"
private const val SERVER_BASE_URL = "http://i13d104.p.ssafy.io:8081/"
    private fun loggingInterceptor() =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    // 1) Kakao Client
    @Provides
    @Singleton
    @Named("KakaoClient")
    fun provideKakaoOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_KEY}")
                    .build()
                Log.d("KakaoHeader", "Authorization → ${req.header("Authorization")}")
                chain.proceed(req)
            }
            .build()

    // 2) Server Client + Authenticator
    @Provides
    @Singleton
    @Named("ServerClient")
    fun provideServerOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val app = PetPlaceApp.getAppContext() as PetPlaceApp

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .addInterceptor { chain ->
                val original = chain.request()
                val token = app.getAccessToken()
                val builder = original.newBuilder()
                if (!token.isNullOrEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .authenticator(object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    // 이미 시도했는데 계속 401이면 무한 루프 방지
                    if (response.request.header("Authorization") != null &&
                        responseCount(response) >= 2
                    ) {
                        return null
                    }

                    val refreshToken = app.getRefreshToken() ?: return null
                    val retrofit = Retrofit.Builder()
                        .baseUrl(SERVER_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(LoginApiService::class.java)

                    return try {
                        val refreshResponse = api.refreshTokenBlocking(
                            LoginApiService.TokenRefreshRequest(refreshToken)
                        ).execute()

                        if (refreshResponse.isSuccessful) {
                            val body = refreshResponse.body()
                            if (body != null && body.success) {
                                app.saveLoginData(
                                    body.accessToken, body.refreshToken,
                                    app.getUserInfo() ?: return null
                                )
                                response.request.newBuilder()
                                    .header("Authorization", "Bearer ${body.accessToken}")
                                    .build()
                            } else null
                        } else null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            })
            .build()
    }

    private fun responseCount(response: Response): Int {
        var res: Response? = response
        var count = 1
        while (res?.priorResponse != null) {
            count++
            res = res.priorResponse
        }
        return count
    }

    // 3) Retrofit 제공
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

    // 4) API 서비스
    @Provides
    @Singleton
    fun provideKakaoApi(
        @Named("Kakao") retrofit: Retrofit
    ): KakaoApiService = retrofit.create(KakaoApiService::class.java)

    @Provides
    @Singleton
    fun provideServerApi(
        @Named("Server") retrofit: Retrofit
    ): LoginApiService = retrofit.create(LoginApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedApi(
        @Named("Server") retrofit: Retrofit
    ): FeedApiService = retrofit.create(FeedApiService::class.java)

    @Provides
    @Singleton
    fun provideImageApi(
        @Named("Server") retrofit: Retrofit
    ): ImageApiService = retrofit.create(ImageApiService::class.java)

    @Provides
    @Singleton
    fun provideJoinApi(
        @Named("Server") retrofit: Retrofit
    ): JoinApiService = retrofit.create(JoinApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApi(
        @Named("Server") retrofit: Retrofit
    ): ChatApiService = retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideHotelApi(
        @Named("Server") retrofit: Retrofit
    ): HotelApiService = retrofit.create(HotelApiService::class.java)

    @Provides
    @Singleton
    fun provideMyPageApi(
        @Named("Server") retrofit: Retrofit
    ): MyPageApiService = retrofit.create(MyPageApiService::class.java)

    @Provides
    @Singleton
    fun provideMissingApi(
        @Named("Server") retrofit: Retrofit
    ): MissingApiService = retrofit.create(MissingApiService::class.java)

    @Provides
    @Singleton
    fun providePyamentApi(
        @Named("Server") retrofit: Retrofit
    ): PaymentsApiService = retrofit.create(PaymentsApiService::class.java)


    @Provides
    @Singleton
    fun provideCaresApi(
        @Named("Server") retrofit: Retrofit
    ): CaresApiService = retrofit.create(CaresApiService::class.java)

    @Provides
    @Singleton
    fun providePetApi(
        @Named("Server") retrofit: Retrofit
    ): PetApiService = retrofit.create(PetApiService::class.java)

    @Provides
    @Singleton
    fun provideTokenApi(
        @Named("Server") retrofit: Retrofit
    ): TokenApiService = retrofit.create(TokenApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApi(
        @Named("Server") retrofit: Retrofit
    ): UserApiService = retrofit.create(UserApiService::class.java)
}
