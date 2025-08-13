package com.example.petplace.presentation.feature.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.feed.CreateImage
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.repository.FeedRepository
import com.example.petplace.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardEditViewModel @Inject constructor(
    private val repo: FeedRepository,
    private val imageRepo: ImageRepository,
) : ViewModel() {

    // 0) 이미지 베이스 (뒤에 슬래시 없이!)
    private val IMAGE_BASE = "http://i13d104.p.ssafy.io:8081"

    // 0-1) UI 표시용: 상대경로면 베이스를 붙여 절대 URL로
    private fun toDisplayUrl(src: String): String =
        if (src.startsWith("http")) src else "$IMAGE_BASE$src"

    // 0-2) 서버 저장용: 베이스가 붙어 있으면 떼고, 항상 "/"부터 시작
    private fun toServerPath(src: String): String {
        var s = src
        if (s.startsWith(IMAGE_BASE)) {
            s = s.removePrefix(IMAGE_BASE)
        }
        if (!s.startsWith("/")) s = "/$s"
        return s
    }

    // 피드 원본 데이터
    private val _feed = MutableStateFlow<FeedRecommendRes?>(null)
    val feed: StateFlow<FeedRecommendRes?> = _feed

    // 입력 상태
    val content = MutableStateFlow("")
    val category = MutableStateFlow("")
    val tagIds = MutableStateFlow<List<Long>>(emptyList())
    val images = MutableStateFlow<List<CreateImage>>(emptyList())

    // 한↔영 카테고리 변환
    val categoryKoToEn = mapOf(
        "내새꾸자랑" to "MYPET",
        "정보" to "INFO",
        "나눔" to "SHARE",
        "후기" to "REVIEW",
        "자유" to "ANY"
    )

    /** 상세 불러오기: UI 상태에는 '표시용 절대 URL'로 보관 */
    fun loadFeedDetail(feedId: Long) {
        viewModelScope.launch {
            val detail = repo.getFeedDetail(feedId)
            _feed.value = detail

            val categoryValue = categoryKoToEn[detail.category] ?: detail.category
            content.value = detail.content
            category.value = categoryValue
            tagIds.value = detail.tags?.map { it.id.toLong() } ?: emptyList()

            val mapped = detail.images
                ?.sortedBy { it.sort }
                ?.mapIndexed { index, img ->
                    CreateImage(
                        src = toDisplayUrl(img.src),   // ← 표시용 절대 URL
                        sort = index + 1
                    )
                } ?: emptyList()
            images.value = mapped
        }
    }

    /** 이미지 업로드(추가) */
    fun appendUploadImages(uris: List<Uri>, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val urls = imageRepo.uploadImages(uris)
                val start = images.value.size + 1
                images.value = images.value + urls.mapIndexed { i, url ->
                    // ★ UI 상태에는 항상 절대 URL
                    CreateImage(src = toDisplayUrl(url), sort = start + i)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // 값 업데이트 함수들
    fun updateContent(newContent: String) { content.value = newContent }
    fun pickCategory(newCategory: String) { category.value = newCategory }
    fun toggleTag(tagId: Long) {
        tagIds.value = if (tagIds.value.contains(tagId)) tagIds.value - tagId else tagIds.value + tagId
    }
    fun removeImage(idx: Int) {
        images.value = images.value.toMutableList().apply { if (idx in indices) removeAt(idx) }
    }

    fun saveEdits(feedId: Long, regionId: Long, onSuccess: () -> Unit, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                // 1) 아직 로컬 Uri(content://, file://)가 images에 남아있다면 업로드
                val sorted = images.value.sortedBy { it.sort }
                val localIdxList = sorted.withIndex()
                    .filter { it.value.src.startsWith("content://") || it.value.src.startsWith("file://") }
                    .map { it.index }

                if (localIdxList.isNotEmpty()) {
                    val localUris = localIdxList.map { android.net.Uri.parse(sorted[it].src) }
                    val uploaded = imageRepo.uploadImages(localUris) // 서버가 준 URL(상대/절대 섞일 수 있음)

                    var upIdx = 0
                    val replaced = sorted.map { item ->
                        if (item.src.startsWith("content://") || item.src.startsWith("file://")) {
                            // ★ UI 상태에는 절대 URL로 세팅
                            item.copy(src = toDisplayUrl(uploaded[upIdx++]))
                        } else item
                    }
                    images.value = replaced
                }

                // 2) 수정 요청
                val body = FeedCreateReq(
                    content = content.value,
                    regionId = regionId,
                    category = category.value,
                    tagIds = tagIds.value,
                    images = images.value
                        .sortedBy { it.sort }
                        .map { it.copy(src = toServerPath(it.src)) }  // ← 핵심!
                )
                repo.editFeed(feedId, body)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

}
