package com.example.petplace.presentation.feature.Neighborhood

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NeighborhoodViewModel : ViewModel() {

    /* --- 고정 데이터 --- */
    val tags = listOf("#식당", "#카페", "#병원", "#용품샵", "#동물병원")

    /* --- UI 상태 --- */
    private val _selectedTag = MutableStateFlow("#식당")
    val selectedTag = _selectedTag.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(true)
    val showBottomSheet = _showBottomSheet.asStateFlow()

    private val _showThanksDialog = MutableStateFlow(false)
    val showThanksDialog = _showThanksDialog.asStateFlow()

    /* --- 상태 변경 함수 --- */
    fun selectTag(tag: String)            { _selectedTag.value = tag }
    fun hideBottomSheet()                 { _showBottomSheet.value = false }
    fun setThanksDialog(visible: Boolean) { _showThanksDialog.value = visible }
}
