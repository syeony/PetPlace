package com.example.petplace.presentation.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.feed.CreateImage
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardEditViewModel @Inject constructor(
    private val repo: FeedRepository
) : ViewModel() {

    // 피드 원본 데이터
    private val _feed = MutableStateFlow<FeedRecommendRes?>(null)
    val feed: StateFlow<FeedRecommendRes?> = _feed

    // 입력 상태
    val content = MutableStateFlow("")
    val category = MutableStateFlow("")
    val tagIds = MutableStateFlow<List<Long>>(emptyList())
    val images = MutableStateFlow<List<CreateImage>>(emptyList())

    // 1. 변환 테이블 추가
    val categoryKoToEn = mapOf(
        "내새꾸자랑" to "MYPET",
        "정보" to "INFO",
        "나눔" to "SHARE",
        "후기" to "REVIEW",
        "자유" to "ANY"
    )
    val categoryEnToKo = categoryKoToEn.entries.associate { (k, v) -> v to k }

    // 2. feed 불러올 때 한글→영문 변환
    fun loadFeedDetail(feedId: Long) {
        viewModelScope.launch {
            val detail = repo.getFeedDetail(feedId)
            _feed.value = detail
            val categoryValue = categoryKoToEn[detail.category] ?: detail.category
            content.value = detail.content
            category.value = categoryValue
            tagIds.value = detail.tags?.map { it.id.toLong() }!!
            images.value = detail.images?.map { CreateImage(src = "http://i13d104.p.ssafy.io:8081"+it.src, sort = it.sort) } ?: emptyList()
        }
    }

    // 값 업데이트 함수들 (BoardWriteViewModel 참고해서 동일하게)
    fun updateContent(newContent: String) {
        content.value = newContent
    }
    fun pickCategory(newCategory: String) {
        category.value = newCategory
    }
    fun toggleTag(tagId: Long) {
        tagIds.value = if (tagIds.value.contains(tagId)) tagIds.value - tagId else tagIds.value + tagId
    }
    fun setImages(imgs: List<CreateImage>) {
        images.value = imgs
    }
    fun removeImage(idx: Int) {
        images.value = images.value.toMutableList().apply { removeAt(idx) }
    }

    // 피드 수정
    fun editFeed(feedId: Long, regionId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.editFeed(
                    feedId,
                    FeedCreateReq(
                        content = content.value,
                        regionId = regionId,
                        category = category.value,
                        tagIds = tagIds.value,
                        images = images.value
                    )
                )
                onSuccess()
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }
}
