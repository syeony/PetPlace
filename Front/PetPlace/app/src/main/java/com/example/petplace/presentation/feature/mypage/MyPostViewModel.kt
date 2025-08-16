package com.example.petplace.presentation.feature.mypage

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.feed.CommentReq
import com.example.petplace.data.model.feed.CommentRes
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.FeedRepository
import com.example.petplace.data.repository.MyPageRepository
import com.example.petplace.presentation.feature.feed.FeedEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Post 데이터 클래스
data class Post(
    val id: Long = 0,
    val category: String,
    val title: String,
    val body: String,
    val meta: String, // 날짜 or 기타 메타 정보
    val imageRes: Int,
    val commentCount: Int
)

// 카테고리별 색상 스타일
val categoryStyles = mapOf(
    "잡담" to Pair(Color(0xFFFFF3E0), Color(0xFFF57C00)),
    "질문" to Pair(Color(0xFFE3F2FD), Color(0xFF1976D2)),
    "정보" to Pair(Color(0xFFE8F5E9), Color(0xFF388E3C))
)


@HiltViewModel
class MyPostViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val feedRepository: FeedRepository // 👍 좋아요/댓글 API 호출용
) : ViewModel() {

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()   // 👈 추가

    data class MyPostUiState(
        val posts: List<FeedRecommendRes> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRefreshing: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyPostUiState())
    val uiState: StateFlow<MyPostUiState> = _uiState.asStateFlow()

    private val _likedFeeds = MutableStateFlow<Set<Long>>(emptySet())
    val likedFeeds: StateFlow<Set<Long>> = _likedFeeds

    private val _commentList = MutableStateFlow<List<CommentRes>>(emptyList())
    val commentList: StateFlow<List<CommentRes>> = _commentList

    init {
        loadMyPosts()

        // ✅ 여기서 FeedEvents 수집
        viewModelScope.launch {
            FeedEvents.commentDelta.collect { (feedId, delta) ->
                bumpCommentCount(feedId, delta)
            }
        }
    }

    fun toggleLike(feed: FeedRecommendRes) {
        viewModelScope.launch {
            val newLiked = !(feed.liked == true || _likedFeeds.value.contains(feed.id))
            updateFeedLikeState(feed.id, newLiked)

            try {
                if (newLiked) feedRepository.likeFeed(feed.id)
                else feedRepository.unlikeFeed(feed.id)
            } catch (e: Exception) {
                updateFeedLikeState(feed.id, !newLiked) // 롤백
            }
        }
    }

    private fun updateFeedLikeState(feedId: Long, newLiked: Boolean) {
        _likedFeeds.update { if (newLiked) it + feedId else it - feedId }
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == feedId) post.copy(
                        likes = post.likes + if (newLiked) 1 else -1,
                        liked = newLiked
                    ) else post
                }
            )
        }
    }

    fun refreshComments(feedId: Long) {
        viewModelScope.launch {
            _commentList.value = feedRepository.fetchComments(feedId)
        }
    }

    // MyPostViewModel
    fun bumpCommentCount(feedId: Long, delta: Int) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == feedId) post.copy(
                        commentCount = (post.commentCount ?: 0) + delta
                    ) else post
                }
            )
        }
    }

    suspend fun addComment(feedId: Long, parentCommentId: Long?, content: String) {
        feedRepository.createComment(CommentReq(feedId, parentCommentId, content))
        refreshComments(feedId)
        // 댓글 수 반영
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == feedId) post.copy(commentCount = post.commentCount + 1) else post
                }
            )
        }
    }

    suspend fun removeComment(commentId: Long, feedId: Long) {
        feedRepository.deleteComment(commentId)
        refreshComments(feedId)
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == feedId) post.copy(commentCount = post.commentCount - 1) else post
                }
            )
        }
    }

    fun loadMyPosts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                myPageRepository.getMyPosts()
                    .onSuccess { posts ->
                        _uiState.value = _uiState.value.copy(
                            posts = posts,
                            isLoading = false
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "게시글을 불러오는 중 오류가 발생했습니다.",
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "게시글을 불러오는 중 오류가 발생했습니다.",
                    isLoading = false
                )
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)

                myPageRepository.getMyPosts()
                    .onSuccess { posts ->
                        _uiState.value = _uiState.value.copy(
                            posts = posts,
                            isRefreshing = false
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "게시글을 새로고침하는 중 오류가 발생했습니다.",
                            isRefreshing = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "게시글을 새로고침하는 중 오류가 발생했습니다.",
                    isRefreshing = false
                )
            }
        }
    }

    /** 내 피드 삭제 */
    fun deleteMyFeed(feedId: Long) {
        viewModelScope.launch {
            try {
                // 서버 삭제
                feedRepository.deleteFeed(feedId)

                // UI 즉시 반영
                _uiState.value = _uiState.value.copy(
                    posts = _uiState.value.posts.filterNot { it.id == feedId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "게시글 삭제 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

}