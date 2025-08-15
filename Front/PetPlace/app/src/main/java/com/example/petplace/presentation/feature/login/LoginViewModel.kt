package com.example.petplace.presentation.feature.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.BuildConfig
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.fcm.FcmTokenRequest
import com.example.petplace.data.model.login.KakaoLoginRequest
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.remote.LoginApiService.LoginRequest
import com.example.petplace.data.remote.TokenApiService
import com.example.petplace.data.repository.KakaoRepository
import com.example.petplace.util.CommonUtils.getFcmToken
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val serverApi: LoginApiService,
    private val tokenApi: TokenApiService) : ViewModel() {

    val app = PetPlaceApp.getAppContext() as PetPlaceApp

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    private val _tempToken = MutableStateFlow("")
    val tempToken: StateFlow<String> = _tempToken
    fun login(id: String, pw: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            try {
                val response = serverApi.login(LoginRequest(id, pw))

                if (response.isSuccessful) {
                    val body = response.body()

                    val accessToken = body?.accessToken
                    val refreshToken = body?.refreshToken
                    val user = body?.user

                    if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && user != null) {
                        (PetPlaceApp.getAppContext() as PetPlaceApp)
                            .saveLoginData(accessToken, refreshToken, user)
                        //fcm토큰 넣을곳
                        val fcmToken = getFcmToken()
                        Log.d("FCM", "login: $fcmToken ")
                        fcmToken?.let {
                            FcmTokenRequest(
                                token = it,
                                appVersion = BuildConfig.VERSION_NAME
                            )
                        }?.let { tokenApi.registerFcmToken(it) }
                        _loginState.value = LoginState(isSuccess = true)
                    } else {
                        _loginState.value = LoginState(error = "응답 데이터가 비어있습니다.")
                    }
                }


            } catch (e: Exception) {
                _loginState.value = LoginState(error = e.message ?: "알 수 없는 오류")
            }
        }
    }

    private val _loginResult = MutableStateFlow<String?>(null)
    val loginResult: StateFlow<String?> = _loginResult

    fun kakaoLoginAndSendToServer(context: Context, onNavigateToJoin: (String) -> Unit) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "로그인 실패: $error")
            } else if (token != null) {
                Log.d("KakaoLogin", "로그인 성공: ${token.accessToken}")

                viewModelScope.launch {
                    val success = loginWithKakao(KakaoLoginRequest(accessToken = token.accessToken))

                    if (!success) {
                        if(_tempToken.value.isNullOrEmpty()){
                            Toast.makeText(context, "서버와 통신중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                        else onNavigateToJoin(_tempToken.value)
                    } else {
                        _loginState.value = LoginState(isSuccess = true)
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }



    suspend fun loginWithKakao(request: KakaoLoginRequest): Boolean {
        val resp = serverApi.loginWithKakao(request)
        if (resp.isSuccessful && resp.body() != null) {
            val body = resp.body()!!
            return if (body.status == "EXISTING_USER") {
                app.saveLoginData(body.tokenDto.accessToken, body.tokenDto.refreshToken, body.tokenDto.user)
                //fcm토큰 넣을곳
                val fcmToken = getFcmToken()
                Log.d("FCM", "login: $fcmToken ")
                fcmToken?.let {
                    FcmTokenRequest(
                        token = it,
                        appVersion = BuildConfig.VERSION_NAME
                    )
                }?.let { tokenApi.registerFcmToken(it) }
                true
            } else {
                _tempToken.value = body.tempToken.orEmpty()
                false
            }
        }
        return false
    }


}
