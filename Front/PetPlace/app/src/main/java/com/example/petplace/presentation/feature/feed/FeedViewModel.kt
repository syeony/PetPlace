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
import coil.Coil
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.petplace.PetPlaceApp
import com.example.petplace.R
import com.example.petplace.data.model.feed.CommentReq
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CommentDelta(val feedId: Long, val delta: Int)

object FeedEvents {
    private val _commentDelta = MutableSharedFlow<CommentDelta>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val commentDelta = _commentDelta.asSharedFlow()

    fun emitCommentDelta(feedId: Long, delta: Int) {
        _commentDelta.tryEmit(CommentDelta(feedId, delta))
    }
}

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val repo: FeedRepository
) : ViewModel() {

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    /* ─── 상수 ─── */
    private val USER_ID = userInfo         // ← 로그인 완료되면 Token or DataStore 에서 꺼내 쓰면 됨
    // 페이지네이션 상태
    private var page = 0
    private val size = 5
    private var isPaging = false
    private var endReached = false

    private val _appending = MutableStateFlow(false)          // 👈 추가
    val appending: StateFlow<Boolean> = _appending            // 👈 추가

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

    init {
        // 1) 배치 먼저 트리거 (비동기, 실패 무시)
        runRecommendBatch()

        // 2) 목록 첫 페이지 로드
        loadFirstPage()
    }

    /** 서버 추천 배치 트리거 */
    private fun runRecommendBatch() {
        viewModelScope.launch {
            // 202/200 빈 바디 성공 → 무시, 실패도 앱 흐름 영향 X
            repo.triggerBatch()
                .onFailure { /* 필요시 로그/스낵바 등 */ }
        }
    }


    // 내가 좋아요 누른 피드 id 집합 (앱 단 관리)
    private val _likedFeeds = MutableStateFlow<Set<Long>>(emptySet())
    val likedFeeds: StateFlow<Set<Long>> = _likedFeeds


    // 댓글 리스트 (서버에서 가져온)
    private val _commentList = MutableStateFlow<List<CommentRes>>(emptyList())
    val commentList: StateFlow<List<CommentRes>> = _commentList

    private inline fun updateFeed(feedId: Long, crossinline transform: (FeedRecommendRes) -> FeedRecommendRes) {
        _remoteFeeds.update { list -> list.map { if (it.id == feedId) transform(it) else it } }
        applyFilters()
    }

    private val baseUrl = "http://i13d104.p.ssafy.io:8081"

    private suspend fun prefetchImages2(urls: List<String>) {
        if (urls.isEmpty()) return
        val loader = Coil.imageLoader(app)
        withContext(Dispatchers.IO) {
            urls.distinct().forEach { raw ->
                val full = if (raw.startsWith("http")) raw else (baseUrl + raw)
                val req = ImageRequest.Builder(app)
                    .data(full)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(req) // ✅ execute() → enqueue() (논블로킹)
            }
        }
    }

    // 이미지들 캐시에 먼저 담아두기
    private suspend fun prefetchImages(urls: List<String>) {
        if (urls.isEmpty()) return
        val loader = Coil.imageLoader(app)
        withContext(Dispatchers.IO) {
            urls.distinct().forEach { raw ->
                val full = if (raw.startsWith("http")) raw else (baseUrl + raw)
                val req = ImageRequest.Builder(app)
                    .data(full)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.execute(req)                              // 동기 실행: 캐시될 때까지 대기
            }
        }
    }

    // ✅ null 방지용 확장 함수(필요한 필드만 넣어도 됨)
    private fun FeedRecommendRes.safeCopy(
        likes: Int? = null,
        liked: Boolean? = null,
        commentCount: Int? = null
    ): FeedRecommendRes {
        return this.copy(
            // 리스트/값이 null로 들어오면 안전한 기본값으로
            comments = this.comments ?: emptyList(),
            tags     = this.tags     ?: emptyList(),
            images   = this.images   ?: emptyList(),
            likes        = likes        ?: (this.likes ?: 0),
            liked        = liked        ?: (this.liked ?: false),
            commentCount = commentCount ?: (this.commentCount ?: 0),
        )
    }

    private fun updateFeedLikeState(feedId: Long, newLiked: Boolean) {
        _likedFeeds.update { if (newLiked) it + feedId else it - feedId }
        updateFeed(feedId) { f ->
            val newLikes = (f.likes ?: 0) + if (newLiked) 1 else -1
            f.safeCopy(likes = newLikes, liked = newLiked)   // ✅ 안전한 copy
        }
    }

    private fun updateFeedCommentDelta(feedId: Long, delta: Int) {
        updateFeed(feedId) { f ->
            val now = (f.commentCount ?: 0) + delta
            f.safeCopy(commentCount = now)                   // ✅ 안전한 copy
        }
    }

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
            val currentlyLiked = (feed.liked == true) || _likedFeeds.value.contains(feed.id)
            val newLiked = !currentlyLiked

            // 1) 낙관적 반영
            updateFeedLikeState(feed.id, newLiked)

            // 2) 서버 호출
            try {
                if (newLiked) repo.likeFeed(feed.id) else repo.unlikeFeed(feed.id)
            } catch (e: Exception) {
                // 3) 실패 시 롤백
                updateFeedLikeState(feed.id, currentlyLiked)
                // 선택: _error.value = "좋아요 실패: ${e.message}"
            }
        }
    }


    // 댓글 작성
    suspend fun addComment(feedId: Long, parentCommentId: Long?, content: String): CommentRes {
        // 1) 서버 작성
        val result = repo.createComment(CommentReq(feedId, parentCommentId, content))
        // 2) 카운트 +1을 즉시 반영
        updateFeedCommentDelta(feedId, +1)
        // 3) 상세 댓글 리스트 새로고침 (목록 하단에 반영)
        refreshComments(feedId)
        FeedEvents.emitCommentDelta(feedId, +1)    // ✅ 추가
        return result
    }

    // 댓글 삭제
    suspend fun removeComment(commentId: Long, feedId: Long) {
        // 1) 서버 삭제
        repo.deleteComment(commentId)
        // 2) 카운트 -1 즉시 반영
        updateFeedCommentDelta(feedId, -1)
        // 3) 상세 댓글 리스트 새로고침
        refreshComments(feedId)
        FeedEvents.emitCommentDelta(feedId, -1)    // ✅ 추가
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

    /** 최초 페이지 로드 (reset) */
    fun loadFirstPage() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            page = 0
            endReached = false

            val result = repo.fetchRecommendedFeeds2(page, size)

            // ✅ 먼저 화면에 반영 (이미지 안 떠도 즉시 목록 표시)
            _remoteFeeds.value = result
            _likedFeeds.value = result.filter { it.liked == true }.map { it.id }.toSet()
            endReached = result.size < size
            applyFilters()
        } catch (e: Exception) {
            _error.value = e.message ?: "알 수 없는 오류"
        } finally {
            _loading.value = false // ✅ 프리패치와 무관하게 바로 off
        }

        // ✅ 프리패치는 뒤에서 조용히
        val urls = _remoteFeeds.value.flatMap { it.images ?: emptyList() }.map { it.src }
        viewModelScope.launch(Dispatchers.IO) {
            prefetchImages2(urls)
        }
    }


    /** 다음 페이지 로드 (append) */
    fun loadNextPage() {
        if (isPaging || endReached) return
        isPaging = true
        _appending.value = true                // 👈 추가 (리스트 하단 로더 on)

        viewModelScope.launch {
            try {
                val next = page + 1
                // 다음 페이지 로드
                val result = repo.fetchRecommendedFeeds2(next, size)
// 1) 중복 제거된 신규만 뽑기
                val existing = _remoteFeeds.value
                val existIds = existing.asSequence().map { it.id }.toHashSet()
                val onlyNew  = result.filter { it.id !in existIds }

// 2) 신규 이미지 먼저 캐시
                prefetchImages(onlyNew.flatMap { it.images ?: emptyList() }.map { it.src })

// 3) 그 다음 append
                _remoteFeeds.value = existing + onlyNew

                // liked 세트 갱신
                _likedFeeds.update { set ->
                    set + result.filter { it.liked == true }.map { it.id }.toSet()
                }

                endReached = result.size < size
                if (!endReached) page = next

                applyFilters()
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                isPaging = false
                _appending.value = false       // 👈 추가 (리스트 하단 로더 off)
            }
        }
    }


    /** 새로고침 (맨 처음부터 다시) */
    fun refreshFeeds(onFinish: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                page = 0
                endReached = false

                val result = repo.fetchRecommendedFeeds2(page, size)
                _remoteFeeds.value = result
                _likedFeeds.value = result.filter { it.liked == true }.map { it.id }.toSet()
                endReached = result.size < size

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