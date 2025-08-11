package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.data.model.feed.CommentReq
import com.example.petplace.data.model.feed.CommentRes
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

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    /* ─── 상수 ─── */
    private val USER_ID = userInfo         // ← 로그인 완료되면 Token or DataStore 에서 꺼내 쓰면 됨
    private val PAGE    = 0
    private val SIZE    = 20

    /* ─── UI State ─── */
    val allCategories = listOf("내새꾸자랑", "정보", "나눔", "후기", "자유")

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

    // 내가 좋아요 누른 피드 id 집합 (앱 단 관리)
    private val _likedFeeds = MutableStateFlow<Set<Long>>(emptySet())
    val likedFeeds: StateFlow<Set<Long>> = _likedFeeds

    // 댓글 리스트
//    private val _commentList = MutableStateFlow<List<CommentRes>>(emptyList())
//    val commentList: StateFlow<List<CommentRes>> = _commentList

    // 댓글 리스트 (서버에서 가져온)
    private val _commentList = MutableStateFlow<List<CommentRes>>(emptyList())
    val commentList: StateFlow<List<CommentRes>> = _commentList

    /** 피드별 댓글 새로고침 */
    fun refreshComments(feedId: Long) {
        viewModelScope.launch {
            try {
                val comments = repo.fetchComments(feedId)
                _commentList.value = comments
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }

    /** 피드 삭제 */
    fun deleteFeed(feedId: Long) {
        viewModelScope.launch {
            try {
                repo.deleteFeed(feedId)
                _remoteFeeds.update { feeds -> feeds.filterNot { it.id == feedId } }
                applyFilters() // 화면에 즉시 반영
            } catch (e: Exception) {
                // 에러처리(선택)
            }
        }
    }

    fun isFeedLiked(feedId: Long) = _likedFeeds.value.contains(feedId)

    fun toggleLike(feed: FeedRecommendRes) {
        viewModelScope.launch {
            try {
                val newLiked = !(feed.liked == true)
                if (newLiked) repo.likeFeed(feed.id)
                else        repo.unlikeFeed(feed.id)

                // 1) remote list 반영
                _remoteFeeds.update { list ->
                    list.map {
                        if (it.id == feed.id) it.copy(liked = newLiked,
                            likes = it.likes + if (newLiked) +1 else -1)
                        else it
                    }
                }
                // 2) _likedFeeds 집합에도 반영
                _likedFeeds.update {
                    if (newLiked) it + feed.id else it - feed.id
                }

                applyFilters()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 댓글 작성
    suspend fun addComment(feedId: Long, parentCommentId: Long?, content: String): CommentRes {
        val result = repo.createComment(
            CommentReq(feedId = feedId, parentCommentId = parentCommentId, content = content)
        )
        refreshComments(feedId) // 댓글 작성 후 바로 새로고침
        return result
    }

    // 댓글 삭제
    suspend fun removeComment(commentId: Long, feedId: Long) {
        repo.deleteComment(commentId)
        refreshComments(feedId) // 삭제 후 바로 새로고침
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

    private fun loadFeeds() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            val result = repo.fetchRecommendedFeeds(USER_ID, PAGE, SIZE)
            // 1) 원본 피드 리스트 반영
            _remoteFeeds.value = result
            // 2) 서버가 반환한 liked 상태로 _likedFeeds 초기화
            _likedFeeds.value = result
                .filter { it.liked == true }
                .map { it.id }
                .toSet()
            applyFilters()
        } catch (e: Exception) {
            _error.value = e.message ?: "알 수 없는 오류"
        } finally {
            _loading.value = false
        }
    }

    fun refreshFeeds(onFinish: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repo.fetchRecommendedFeeds(USER_ID, PAGE, SIZE)
                _remoteFeeds.value = result
                // 여기에도 반드시!
                _likedFeeds.value = result
                    .filter { it.liked == true }
                    .map { it.id }
                    .toSet()
                applyFilters()
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _loading.value = false
                onFinish()
            }
        }
    }
}

/* 작고 반복되는 프로필 이미지 렌더링 */
@Composable
fun ProfileImage(url: String?) {
    val painter = url?.let { rememberAsyncImagePainter("http://i13d104.p.ssafy.io:8081"+it) }
        ?: painterResource(R.drawable.pp_logo)

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(35.dp)
            .clip(CircleShape)
    )
}
