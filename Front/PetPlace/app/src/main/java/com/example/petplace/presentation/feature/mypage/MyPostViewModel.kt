package com.example.petplace.presentation.feature.mypage

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.R
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val myPageRepository: MyPageRepository
) : ViewModel() {
    data class MyPostUiState(
        val posts: List<FeedRecommendRes> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRefreshing: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyPostUiState())
    val uiState: StateFlow<MyPostUiState> = _uiState.asStateFlow()

    init {
        loadMyPosts()
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

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // postRepository.deletePost(postId)

                val updatedPosts = _uiState.value.posts.filter { it.id != postId }
                _uiState.value = _uiState.value.copy(posts = updatedPosts)

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

    // 샘플 더미 데이터 - 실제 구현에서는 제거
    private fun getSamplePosts(): List<Post> {
        return listOf(
            Post(
                id = 1,
                category = "잡담",
                title = "오늘 날씨 너무 좋네요",
                body = "하늘이 맑고 바람도 시원해서 산책하기 딱 좋은 날씨입니다.",
                meta = "2025.08.10",
                imageRes = R.drawable.pp_logo,
                commentCount = 5
            ),
            Post(
                id = 2,
                category = "질문",
                title = "Jetpack Compose 리스트 간격 조절 질문",
                body = "LazyColumn에서 아이템 간 간격을 조절하는 방법을 알고 싶습니다.",
                meta = "2025.08.09",
                imageRes = R.drawable.pp_logo,
                commentCount = 8
            ),
            Post(
                id = 3,
                category = "정보",
                title = "안드로이드 스튜디오 최신 단축키 모음",
                body = "효율적으로 개발할 수 있는 단축키 리스트를 정리했습니다.",
                meta = "2025.08.08",
                imageRes = R.drawable.pp_logo,
                commentCount = 12
            )
        )
    }
}