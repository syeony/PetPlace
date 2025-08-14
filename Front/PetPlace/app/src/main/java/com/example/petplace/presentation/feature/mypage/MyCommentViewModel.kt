package com.example.petplace.presentation.feature.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.MyPageRepository
import com.example.petplace.data.model.feed.CommentRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MyCommentInfo(
    val id: Long,
    val content: String,
    val authorName: String,
    val authorProfileImage: String?,
    val timeAgo: String,
    val originalPostId: Long,
    val originalPostTitle: String,  // 이 정보도 CommentRes에 없으므로 별도로 처리 필요
    val createdAt: String
)

data class MyCommentUiState(
    val comments: List<MyCommentInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyCommentViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCommentUiState())
    val uiState: StateFlow<MyCommentUiState> = _uiState.asStateFlow()

    fun loadMyComments() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                myPageRepository.getMyComments()
                    .onSuccess { comments ->
                        val commentInfoList = comments.map { comment ->
                            MyCommentInfo(
                                id = comment.id,
                                content = comment.content,
                                authorName = comment.userNick,
                                authorProfileImage = comment.userImg,
                                timeAgo = formatTimeAgo(comment.createdAt),
                                originalPostId = comment.feedId,
                                originalPostTitle = "게시글 제목", // CommentRes에 게시글 제목이 없으므로 기본값 또는 별도 조회 필요
                                createdAt = comment.createdAt
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            comments = commentInfoList,
                            isLoading = false
                        )
                    }
                    .onFailure { exception ->
                        Log.e("MyCommentViewModel", "댓글 로드 실패", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "댓글을 불러오는데 실패했습니다",
                            isLoading = false
                        )
                    }

            } catch (e: Exception) {
                Log.e("MyCommentViewModel", "댓글 로드 중 오류", e)
                _uiState.value = _uiState.value.copy(
                    error = "댓글을 불러오는데 실패했습니다",
                    isLoading = false
                )
            }
        }
    }

    private fun formatTimeAgo(createdAt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(createdAt)
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)

            val minutes = diffInMillis / (1000 * 60)
            val hours = diffInMillis / (1000 * 60 * 60)
            val days = diffInMillis / (1000 * 60 * 60 * 24)

            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                days < 30 -> "${days}일 전"
                else -> {
                    val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                    outputFormat.format(date ?: Date())
                }
            }
        } catch (e: Exception) {
            Log.e("MyCommentViewModel", "시간 포맷 변환 오류", e)
            "시간 정보 없음"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}