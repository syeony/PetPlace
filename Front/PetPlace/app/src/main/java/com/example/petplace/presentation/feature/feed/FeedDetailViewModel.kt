package com.example.petplace.presentation.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
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
class FeedDetailViewModel @Inject constructor(
    private val repo: FeedRepository
) : ViewModel() {

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    // UI 상태
    private val _feed = MutableStateFlow<FeedRecommendRes?>(null)
    val feed: StateFlow<FeedRecommendRes?> = _feed

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 내가 좋아요 누른 피드 id 집합
    private val _likedFeeds = MutableStateFlow<Set<Long>>(emptySet())
    val likedFeeds: StateFlow<Set<Long>> = _likedFeeds

    // 댓글 리스트 (서버에서 가져온)
    private val _commentList = MutableStateFlow<List<CommentRes>>(emptyList())
    val commentList: StateFlow<List<CommentRes>> = _commentList

    /** 피드 상세 정보 로드 */
    fun loadFeedDetail(feedId: Long) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val feedDetail = repo.getFeedDetail(feedId)
                _feed.value = feedDetail

                // 좋아요 상태 업데이트
                if (feedDetail.liked == true) {
                    _likedFeeds.value = _likedFeeds.value + feedDetail.id
                }

                // 댓글 목록도 함께 로드
                refreshComments(feedId)

            } catch (e: Exception) {
                _error.value = e.message ?: "피드를 불러올 수 없습니다"
            } finally {
                _loading.value = false
            }
        }
    }

    /** 좋아요 토글 */
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
                _error.value = "좋아요 처리에 실패했습니다"
            }
        }
    }

    /** 피드 삭제 */
    fun deleteFeed(feedId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.deleteFeed(feedId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "피드 삭제에 실패했습니다"
            }
        }
    }

    /** 피드별 댓글 새로고침 */
    fun refreshComments(feedId: Long) {
        viewModelScope.launch {
            try {
                val comments = repo.fetchComments(feedId)
                _commentList.value = comments
            } catch (e: Exception) {
                // 댓글 로드 실패는 조용히 처리
            }
        }
    }

    /** 댓글 작성 */
    suspend fun addComment(feedId: Long, parentCommentId: Long?, content: String): CommentRes {
        // 1) 서버 작성
        val result = repo.createComment(CommentReq(feedId, parentCommentId, content))

        // 2) 카운트 +1을 즉시 반영
        updateFeedCommentDelta(feedId, +1)

        // 3) 상세 댓글 리스트 새로고침
        refreshComments(feedId)

        return result
    }

    /** 댓글 삭제 */
    suspend fun removeComment(commentId: Long, feedId: Long) {
        // 1) 서버 삭제
        repo.deleteComment(commentId)

        // 2) 카운트 -1 즉시 반영
        updateFeedCommentDelta(feedId, -1)

        // 3) 상세 댓글 리스트 새로고침
        refreshComments(feedId)
    }

    // Private helper functions
    private fun updateFeedLikeState(feedId: Long, newLiked: Boolean) {
        _likedFeeds.update { if (newLiked) it + feedId else it - feedId }

        _feed.update { currentFeed ->
            if (currentFeed?.id == feedId) {
                val newLikes = (currentFeed.likes ?: 0) + if (newLiked) 1 else -1
                currentFeed.safeCopy(likes = newLikes, liked = newLiked)
            } else {
                currentFeed
            }
        }
    }

    private fun updateFeedCommentDelta(feedId: Long, delta: Int) {
        _feed.update { currentFeed ->
            if (currentFeed?.id == feedId) {
                val newCount = (currentFeed.commentCount ?: 0) + delta
                currentFeed.safeCopy(commentCount = newCount)
            } else {
                currentFeed
            }
        }
    }

    // null 방지용 확장 함수
    private fun FeedRecommendRes.safeCopy(
        likes: Int? = null,
        liked: Boolean? = null,
        commentCount: Int? = null
    ): FeedRecommendRes {
        return this.copy(
            comments = this.comments ?: emptyList(),
            tags = this.tags ?: emptyList(),
            images = this.images ?: emptyList(),
            likes = likes ?: (this.likes ?: 0),
            liked = liked ?: (this.liked ?: false),
            commentCount = commentCount ?: (this.commentCount ?: 0),
        )
    }
}