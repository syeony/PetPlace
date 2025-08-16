package com.example.petplace.presentation.feature.mypage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyLikePostViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val feedRepository: com.example.petplace.data.repository.FeedRepository, // ✅ 추가
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class MyLikePostUiState(
        val likedPosts: List<FeedRecommendRes> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRefreshing: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyLikePostUiState())
    val uiState: StateFlow<MyLikePostUiState> = _uiState.asStateFlow()

    val app = context as PetPlaceApp
    val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    init {
        loadMyLikedPosts()

        // ✅ 댓글 수 증감 이벤트 반영
        viewModelScope.launch {
            com.example.petplace.presentation.feature.feed.FeedEvents.commentDelta.collect { (feedId, delta) ->
                bumpCommentCount(feedId, delta)
            }
        }
    }

    fun isMine(authorUserId: Long?): Boolean {
        val myId = user.userId
        return (authorUserId != null && myId != null && authorUserId == myId)
    }

    fun loadMyLikedPosts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                myPageRepository.getMyLikedPosts()
                    .onSuccess { likedPosts ->
                        _uiState.value = _uiState.value.copy(
                            likedPosts = likedPosts,
                            isLoading = false
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            error = e.message ?: "찜한 게시글을 불러오는 중 오류가 발생했습니다.",
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "찜한 게시글을 불러오는 중 오류가 발생했습니다.",
                    isLoading = false
                )
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
                myPageRepository.getMyLikedPosts()
                    .onSuccess { likedPosts ->
                        _uiState.value = _uiState.value.copy(
                            likedPosts = likedPosts,
                            isRefreshing = false
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            error = e.message ?: "찜한 게시글을 새로고침하는 중 오류가 발생했습니다.",
                            isRefreshing = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "찜한 게시글을 새로고침하는 중 오류가 발생했습니다.",
                    isRefreshing = false
                )
            }
        }
    }

    /** ✅ 좋아요 토글 (낙관적 갱신). 찜 해제 시 목록에서 제거 */
    fun toggleLike(feed: FeedRecommendRes) {
        viewModelScope.launch {
            val wasLiked = feed.liked == true
            // 1) 낙관적 UI 업데이트
            val optimisticallyUpdated = _uiState.value.likedPosts.map {
                if (it.id == feed.id) {
                    val newLiked = !wasLiked
                    val newLikes = (it.likes ?: 0) + if (newLiked) 1 else -1
                    it.copy(liked = newLiked, likes = newLikes)
                } else it
            }.let { list ->
                // 찜 목록 화면 특성상: 찜 해제하면 리스트에서 제거
                if (wasLiked) list.filterNot { it.id == feed.id } else list
            }
            _uiState.value = _uiState.value.copy(likedPosts = optimisticallyUpdated)

            // 2) 서버 반영
            try {
                if (wasLiked) feedRepository.unlikeFeed(feed.id) else feedRepository.likeFeed(feed.id)
            } catch (e: Exception) {
                // 3) 실패 시 롤백
                // (원래 상태로 되돌리기 위해 다시 반대 연산)
                val rolledBack = if (wasLiked) {
                    // 원래 liked=true였는데 unlike 실패 → 다시 넣기
                    val original = feed.copy(liked = true, likes = (feed.likes ?: 0))
                    listWithInsertOrReplace(_uiState.value.likedPosts, original)
                } else {
                    // 원래 liked=false였는데 like 실패 → 좋아요 해제
                    _uiState.value.likedPosts.map {
                        if (it.id == feed.id) it.copy(liked = false, likes = (feed.likes ?: 0)) else it
                    }
                }
                _uiState.value = _uiState.value.copy(
                    likedPosts = rolledBack,
                    error = e.message ?: "좋아요 처리 중 오류가 발생했습니다."
                )
            }
        }
    }

    /** ✅ 댓글 수 증감 반영 */
    private fun bumpCommentCount(feedId: Long, delta: Int) {
        _uiState.value = _uiState.value.copy(
            likedPosts = _uiState.value.likedPosts.map { p ->
                if (p.id == feedId) p.copy(commentCount = (p.commentCount ?: 0) + delta) else p
            }
        )
    }

    /** ✅ 내 글 삭제 (성공 시 목록에서 제거) */
    fun deleteFeed(feedId: Long) {
        viewModelScope.launch {
            try {
                feedRepository.deleteFeed(feedId)
                _uiState.value = _uiState.value.copy(
                    likedPosts = _uiState.value.likedPosts.filterNot { it.id == feedId }
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

    // --- 유틸 ---
    private fun listWithInsertOrReplace(list: List<FeedRecommendRes>, item: FeedRecommendRes): List<FeedRecommendRes> {
        val idx = list.indexOfFirst { it.id == item.id }
        return if (idx >= 0) list.toMutableList().apply { set(idx, item) }
        else list + item
    }
}