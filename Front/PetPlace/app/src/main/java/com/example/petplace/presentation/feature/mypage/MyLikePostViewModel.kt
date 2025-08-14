package com.example.petplace.presentation.feature.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyLikePostViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository
) : ViewModel() {

    data class MyLikePostUiState(
        val likedPosts: List<FeedRecommendRes> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRefreshing: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyLikePostUiState())
    val uiState: StateFlow<MyLikePostUiState> = _uiState.asStateFlow()

    init {
        loadMyLikedPosts()
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
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "찜한 게시글을 불러오는 중 오류가 발생했습니다.",
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
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "찜한 게시글을 새로고침하는 중 오류가 발생했습니다.",
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

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            try {
                // 좋아요 상태 토글 API 호출
                // TODO: Replace with actual repository call
                // myPageRepository.toggleLike(postId)

                // 로컬 상태 업데이트
                val updatedPosts = _uiState.value.likedPosts.map { post ->
                    if (post.id == postId) {
                        val newLiked = !(post.liked ?: false)
                        val newLikeCount = if (newLiked) {
                            (post.likes ?: 0) + 1
                        } else {
                            (post.likes ?: 0) - 1
                        }
                        post.copy(liked = newLiked, likes = newLikeCount)
                    } else {
                        post
                    }
                }

                _uiState.value = _uiState.value.copy(likedPosts = updatedPosts)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "좋아요 처리 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun removeLikedPost(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // myPageRepository.removeLike(postId)

                val updatedPosts = _uiState.value.likedPosts.filter { it.id != postId }
                _uiState.value = _uiState.value.copy(likedPosts = updatedPosts)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "찜 해제 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}