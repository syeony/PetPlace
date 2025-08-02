package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.feed.CommentDto
import com.example.petplace.data.local.feed.FeedDto
import com.example.petplace.data.local.feed.TagDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardViewModel : ViewModel() {

    /* 화면에 보여줄 카테고리(말풍선) */
    val allCategories = listOf("MYPET", "INFO", "나눔", "공구", "자유")

    /* 하나만 선택(재클릭 → 해제) */
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    /* 더미 피드 데이터 */
    private val _allFeeds      = MutableStateFlow(dummyFeeds)
    private val _filteredFeeds = MutableStateFlow(dummyFeeds)
    val          filteredFeeds : StateFlow<List<FeedDto>> = _filteredFeeds

    init { applyFilters() }

    /* ------------ 카테고리 토글 ------------ */
    fun toggleCategory(cat: String) {
        _selectedCategory.update { if (it == cat) null else cat }
        applyFilters()
    }

    /* ------------ 검색어 ------------ */
    fun updateSearchText(t: String) {
        _searchText.value = t
        applyFilters()
    }

    /* ------------ 필터링 ------------ */
    private fun applyFilters() = viewModelScope.launch {
        val cat   = _selectedCategory.value
        val query = _searchText.value.lowercase()

        _filteredFeeds.value = _allFeeds.value.filter { feed ->
            (cat == null || feed.category == cat) &&
                    (query.isBlank() || feed.content.lowercase().contains(query))
        }
    }

    /* ------------ 댓글 ------------ */
    fun getCommentsForFeed(feedId: Long): List<CommentDto> =
        _allFeeds.value.first { it.id == feedId }.comments
}

/* 작고 반복되는 프로필 이미지 렌더링 */
@Composable
fun ProfileImage(url: String?) {
    val painter = url?.let { rememberAsyncImagePainter(it) }
        ?: painterResource(R.drawable.pp_logo)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(35.dp)
            .clip(CircleShape)
    )
}

/* --------------------------- 더미 10개 ---------------------------- */

private const val TS = "2025-08-01T14:51:19"          // 타임스탬프 고정

private val userChulsoo = Triple(1L, "철수", null)
private val userYounghee= Triple(2L, "영희", null)
private val userMinsu   = Triple(3L, "민수", null)

/* 공통 태그 */
private val tagDog      = TagDto(1,  "강아지")
private val tagWalk     = TagDto(9,  "산책코스")
private val tagSnack    = TagDto(11, "간식추천")
private val tagCute     = TagDto(17, "귀여움주의")
private val tagPhoto    = TagDto(18, "사진공유")
private val tagRabbit   = TagDto(3,  "토끼")
private val tagGoods    = TagDto(6,  "반려동물용품")
private val tagReview   = TagDto(10, "입양후기")
private val tagHealth   = TagDto(8,  "건강관리")
private val tagFoodRv   = TagDto(12, "사료후기")
private val tagCat      = TagDto(2,  "고양이")

// 임시 피드 콘텐츠 사진
private val tempImg = listOf("https://lh4.googleusercontent.com/proxy/d9kCctaZDANtXrlzOCIfN9dV8y0d0wD75pIdJ7RVeebztPErjpoy-oskh3PGWrm8jHuDDhNjMCzzD4PJ1RPFF4HRZckQcCEQfxyMWPQ-",
    "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Felis_catus-cat_on_snow.jpg/640px-Felis_catus-cat_on_snow.jpg")

/* 댓글 샘플 (feed 1번) */
private val feed1Comments = listOf(
    CommentDto(
        id = 1, parentCommentId = null, feedId = 1,
        content   = "초코 너무 귀여워요 🐶",
        userId    = userYounghee.first,
        userNick  = userYounghee.second,
        userImg   = userYounghee.third,
        createdAt = TS,
        replies = listOf(
            CommentDto(
                id = 3, parentCommentId = 1, feedId = 1,
                content   = "감사해요! 한강공원에서 했어요!",
                userId    = userChulsoo.first,
                userNick  = userChulsoo.second,
                userImg   = userChulsoo.third,
                createdAt = TS
            )
        )
    ),
    CommentDto(
        id = 2, parentCommentId = null, feedId = 1,
        content   = "산책 어디서 하셨나요?",
        userId    = userMinsu.first,
        userNick  = userMinsu.second,
        userImg   = userMinsu.third,
        createdAt = TS
    )
)

/* 10 개 피드 */
val dummyFeeds = listOf(
    FeedDto(
        id = 1, content = "우리 초코 산책 다녀왔어요! 너무 귀엽네요 🐶",
        userId = userChulsoo.first, userNick = userChulsoo.second, userImg = userChulsoo.third,
        regionId = 1001, category = "MYPET", createdAt = TS,
        tags = listOf(tagDog, tagWalk, tagSnack),
        commentCount = feed1Comments.size,
        comments = feed1Comments,
        contentImg = tempImg
    ),
    FeedDto(
        id = 4, content = "오늘 초코가 새로운 장난감을 좋아했어요!",
        userId = userChulsoo.first, userNick = userChulsoo.second, userImg = userChulsoo.third,
        regionId = 1001, category = "MYPET", createdAt = TS,
        tags = listOf(tagDog, tagCute)
    ),
    FeedDto(
        id = 7, content = "초코가 처음으로 친구 강아지를 만났어요 🐕",
        userId = userChulsoo.first, userNick = userChulsoo.second, userImg = userChulsoo.third,
        regionId = 1001, category = "MYPET", createdAt = TS,
        tags = listOf(tagDog, tagPhoto)
    ),
    FeedDto(
        id = 21, content = "토토가 오늘 첫 산책을 나갔어요 🐇",
        userId = userMinsu.first, userNick = userMinsu.second, userImg = userMinsu.third,
        regionId = 1003, category = "MYPET", createdAt = TS,
        tags = listOf(tagRabbit, tagGoods)
    ),
    FeedDto(
        id = 26, content = "토토가 너무 귀여워서 사진 찍었어요 📸",
        userId = userMinsu.first, userNick = userMinsu.second, userImg = userMinsu.third,
        regionId = 1003, category = "MYPET", createdAt = TS,
        tags = listOf(tagRabbit, tagReview)
    ),
    FeedDto(
        id = 28, content = "오늘 토토가 제 손을 핥았어요 🥰",
        userId = userMinsu.first, userNick = userMinsu.second, userImg = userMinsu.third,
        regionId = 1003, category = "MYPET", createdAt = TS,
        tags = listOf(tagRabbit, tagPhoto)
    ),
    FeedDto(
        id = 11, content = "우리 나비가 창밖을 보며 하루종일 앉아있네요 🐱",
        userId = userYounghee.first, userNick = userYounghee.second, userImg = userYounghee.third,
        regionId = 1002, category = "MYPET", createdAt = TS,
        tags = listOf(tagCat, tagCute)
    ),
    FeedDto(
        id = 14, content = "나비가 캣타워에서 놀다가 자버렸어요 😺",
        userId = userYounghee.first, userNick = userYounghee.second, userImg = userYounghee.third,
        regionId = 1002, category = "MYPET", createdAt = TS,
        tags = listOf(tagCat, tagHealth, tagFoodRv)
    ),
    FeedDto(
        id = 17, content = "우리 집 냥이가 처음으로 간식 먹어봤어요!",
        userId = userYounghee.first, userNick = userYounghee.second, userImg = userYounghee.third,
        regionId = 1002, category = "MYPET", createdAt = TS,
        tags = listOf(tagCat, tagGoods)
    ),
    FeedDto(
        id = 19, content = "나비가 오늘 너무 귀엽네요 💕",
        userId = userYounghee.first, userNick = userYounghee.second, userImg = userYounghee.third,
        regionId = 1002, category = "MYPET", createdAt = TS,
        tags = listOf(tagCat, tagCute)
    )
)
