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
    private val SIZE    = 100

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

    // 댓글 새로고침(댓글 등록하거나 삭제할때 바로바로 반영)
//    fun refreshComments(feedId: Long) {
//        viewModelScope.launch {
//            try {
//                val comments = repo.getComments(feedId)
//                _commentList.value = comments
//            } catch (e: Exception) {
//                // 에러 처리(토스트 등)
//            }
//        }
//    }

    fun isFeedLiked(feedId: Long) = _likedFeeds.value.contains(feedId)

    fun toggleLike(feed: FeedRecommendRes) {
        viewModelScope.launch {
            try {
                if (isFeedLiked(feed.id)) {
                    // 좋아요 취소
                    repo.unlikeFeed(feed.id) // feed.id 또는 서버에서 받는 likeId
                    _likedFeeds.update { it - feed.id }
                } else {
                    // 좋아요 등록
                    repo.likeFeed(feed.id)
                    _likedFeeds.update { it + feed.id }
                }
                // 최신 좋아요 수 동기화하려면 서버 feedLikes 받아서 feeds 상태 갱신
                refreshLikeCount(feed.id)
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }

    private fun refreshLikeCount(feedId: Long) {
        // 실제로는 feedRecommendRes의 likes도 최신화 필요!
        // _remoteFeeds 업데이트 코드 추가!
        // 아래는 예시. (feedLikes만 바꿔주는 방식)
        _remoteFeeds.update { feeds ->
            feeds.map {
                if (it.id == feedId) it.copy(likes = it.likes + (if (isFeedLiked(feedId)) 1 else -1))
                else it
            }
        }
        applyFilters() // 화면 반영
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

    fun refreshFeeds(onFinish: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val result = repo.fetchRecommendedFeeds(USER_ID, PAGE, SIZE)
                _remoteFeeds.value = result
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
