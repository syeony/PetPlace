package com.example.petplace.presentation.feature.walk_and_care

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WalkAndCareWriteViewModel @Inject constructor() : ViewModel() {

    val categories = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    private val _pickedCat   = MutableStateFlow(categories.first())
    private val _title       = MutableStateFlow("")
    private val _details     = MutableStateFlow("")
    private val _date        = MutableStateFlow<LocalDate?>(null)
    private val _startTime   = MutableStateFlow<LocalTime?>(null)
    private val _endTime     = MutableStateFlow<LocalTime?>(null)
    private val _imageUris   = MutableStateFlow<List<Uri>>(emptyList())

    val pickedCat  : StateFlow<String>           = _pickedCat.asStateFlow()
    val title      : StateFlow<String>           = _title.asStateFlow()
    val details    : StateFlow<String>           = _details.asStateFlow()
    val date       : StateFlow<LocalDate?>       = _date.asStateFlow()
    val startTime  : StateFlow<LocalTime?>       = _startTime.asStateFlow()
    val endTime    : StateFlow<LocalTime?>       = _endTime.asStateFlow()
    val imageUris  : StateFlow<List<Uri>>        = _imageUris.asStateFlow()

    // 버튼 활성화 조건
    val isValid: StateFlow<Boolean> =
        combine(_title, _details) { t, d -> t.isNotBlank() && d.isNotBlank() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // setters
    fun selectCategory(cat: String) { _pickedCat.value = cat }
    fun updateTitle(v: String)      { _title.value = v }
    fun updateDetails(v: String)    { _details.value = v }
    fun setDate(v: LocalDate)       { _date.value = v }
    fun setStartTime(v: LocalTime)  { _startTime.value = v }
    fun setEndTime(v: LocalTime)    { _endTime.value = v }

    fun addImages(uris: List<Uri>) {
        _imageUris.value = (_imageUris.value + uris).distinct().take(5)
    }
    fun removeImage(uri: Uri) {
        _imageUris.value = _imageUris.value - uri
    }
    fun clearImages() { _imageUris.value = emptyList() }

    // 화면 표시용 포맷
    @RequiresApi(Build.VERSION_CODES.O)
    fun dateText(): String =
        _date.value?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) ?: "날짜를 선택해주세요"
    @RequiresApi(Build.VERSION_CODES.O)
    fun startText(): String =
        _startTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "시작 시간"
    @RequiresApi(Build.VERSION_CODES.O)
    fun endText(): String =
        _endTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "종료 시간"
}
