package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.cares.CareDetail
import com.example.petplace.data.repository.CaresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val data: CareDetail? = null
)

@HiltViewModel
class WalkAndCareDetailViewModel @Inject constructor(
    private val caresRepository: CaresRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetailUiState(loading = true)
            caresRepository.detail(id)
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body?.success == true && body.data != null) {
                            _uiState.value = DetailUiState(
                                loading = false,
                                data = body.data
                            )
                        } else {
                            _uiState.value = DetailUiState(
                                loading = false,
                                error = body?.message ?: "상세 데이터를 불러올 수 없습니다."
                            )
                        }
                    } else {
                        _uiState.value = DetailUiState(
                            loading = false,
                            error = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = DetailUiState(
                        loading = false,
                        error = e.message ?: "네트워크 오류"
                    )
                }
        }
    }
}
