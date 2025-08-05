package com.example.petplace.presentation.feature.join

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.KakaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val repo: KakaoRepository
) : ViewModel() {
    var userId = mutableStateOf("")
        private set
    var password = mutableStateOf("")
        private set
    var nickname = mutableStateOf("")
        private set
    fun onUserIdChange(newId: String) { userId.value = newId }
    fun onPasswordChange(newPw: String) { password.value = newPw }
    fun onNicknameChange(newName: String) { nickname.value = newName }

    // 동네 이름 상태 (null: 아직 로딩 전)
    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName

    fun fetchRegionByCoord(lat: Double, lng: Double) {
        viewModelScope.launch {
            _regionName.value = repo.getRegionByCoord(lat, lng)
        }
    }
}
