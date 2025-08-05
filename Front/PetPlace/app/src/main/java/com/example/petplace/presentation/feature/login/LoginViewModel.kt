package com.example.petplace.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.remote.LoginApiService.LoginRequest
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
    private val serverApi: LoginApiService
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(id: String, pw: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            try {
                val response = serverApi.login(LoginRequest(id, pw))

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (!token.isNullOrEmpty()) {
                        // JWT 저장
                        (PetPlaceApp.getAppContext() as PetPlaceApp).saveJwtToken(token)

                        _loginState.value = LoginState(isSuccess = true)
                    } else {
                        _loginState.value = LoginState(error = "토큰이 비어있습니다.")
                    }
                } else {
                    _loginState.value = LoginState(error = "로그인 실패(${response.code()})")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState(error = e.message ?: "알 수 없는 오류")
            }
        }
    }
}
