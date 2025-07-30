package com.example.petplace.presentation.feature.join

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

    // 동네 이름 상태 (null: 아직 로딩 전)
    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName

    /** 위경도 받아서 바로 행정동/법정동 이름 조회 */
    fun fetchRegionByCoord(lat: Double, lng: Double) {
        viewModelScope.launch {
            _regionName.value = repo.getRegionByCoord(lat, lng)
        }
    }
}
