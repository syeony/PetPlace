package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val repo: FeedRepository
) : ViewModel() {

    /* ─── 상수 ─── */
    private val USER_ID = 1L         // ← 로그인 완료되면 Token or DataStore 에서 꺼내 쓰면 됨
    private val PAGE    = 0
    private val SIZE    = 20

    /* ─── UI State ─── */
    val allCategories = listOf("MYPET", "INFO", "나눔", "공구", "자유")

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    /** 서버-원본 */
    private val _remoteFeeds = MutableStateFlow<List<FeedRecommendRes>>(emptyList())

    /** 로컬 필터 결과 */
    private val _filteredFeeds = MutableStateFlow<List<FeedRecommendRes>>(emptyList())
    val filteredFeeds: StateFlow<List<FeedRecommendRes>> = _filteredFeeds

    /** 로딩 & 에러 (선택) */
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadFeeds() }

    /** ------------ 서버 호출 ------------ */
    private fun loadFeeds() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            val result = repo.fetchRecommendedFeeds(USER_ID, PAGE, SIZE)
            _remoteFeeds.value = result
            applyFilters()                 // 원본 들어온 뒤 화면에 반영
        } catch (e: Exception) {
            _error.value = e.message ?: "알 수 없는 오류"
        } finally {
            _loading.value = false
        }
    }

    /** ------------ 카테고리 토글 ------------ */
    fun toggleCategory(cat: String) {
        _selectedCategory.update { if (it == cat) null else cat }
        applyFilters()
    }

    /** ------------ 검색어 업데이트 ------------ */
    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }

    /** ------------ 필터링 ------------ */
    private fun applyFilters() {
        val cat   = _selectedCategory.value
        val query = _searchText.value.trim().lowercase()

        _filteredFeeds.value = _remoteFeeds.value.filter { f ->
            (cat == null || f.category == cat) &&
                    (query.isBlank() || f.content.lowercase().contains(query))
        }
    }

    /** ------------ 댓글 가져오기 ------------ */
    fun getCommentsForFeed(feedId: Long) =
        _remoteFeeds.value.firstOrNull { it.id == feedId }?.comments ?: emptyList()
}

/* 작고 반복되는 프로필 이미지 렌더링 */
@Composable
fun ProfileImage(url: String?) {
    val painter = url?.let { rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.pp_logo)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(35.dp)
            .clip(CircleShape)
    )
}
