package com.example.petplace.presentation.feature.Missing_register

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel : ViewModel() {

    private val _detail = MutableStateFlow("")
    val detail = _detail.asStateFlow()

    private val _imageList = MutableStateFlow<List<Uri>>(emptyList())
    val imageList = _imageList.asStateFlow()

    var date = MutableStateFlow("2024년 01월 15일")
        private set
    var time = MutableStateFlow("오후 14:30")
        private set
    var place = MutableStateFlow("경상북도 구미시 인의동 365-5")
        private set

    /* -------- 업데이트 메서드 -------- */
    fun setDetail(text: String) { _detail.value = text }

    fun addImages(uris: List<Uri>) {
        _imageList.value = (_imageList.value + uris).distinct().take(5) // 최대 5장
    }

    fun clearImages()     { _imageList.value = emptyList() }
}
