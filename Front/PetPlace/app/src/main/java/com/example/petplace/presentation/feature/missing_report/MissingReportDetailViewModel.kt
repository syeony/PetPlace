package com.example.petplace.presentation.feature.missing_report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.missing_report.MissingReportDetailDto
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissingReportDetailViewModel @Inject constructor(
    private val repo: MissingSightingRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val data: MissingReportDetailDto? = null,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun load(id: Long) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            repo.getMissingReportDetail(id)
                .onSuccess { detail ->
                    _ui.update { it.copy(loading = false, data = detail) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message ?: "불러오기 실패") }
                }
        }
    }
}
