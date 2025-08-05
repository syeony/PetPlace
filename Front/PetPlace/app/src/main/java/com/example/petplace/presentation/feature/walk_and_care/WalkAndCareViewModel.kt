package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.local.feed.CommentDto
import com.example.petplace.data.local.feed.FeedDto
import com.example.petplace.data.local.feed.ImgDto
import com.example.petplace.data.local.feed.TagDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkAndCareViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _allPosts = listOf(
        Post("산책구인", "이 카페 좋으네영", "분위기도 좋고 강아지 간식도 줘요 추천합니다", "인의동 · 4시간 전 · 조회 10", 3, R.drawable.pp_logo),
        Post("돌봄구인", "강아지 용품 나눔해요", "새끼 때 쓰던 용품들 필요하신 분께 드려요", "인의동 · 6시간 전 · 조회 25", 7, R.drawable.pp_logo),
        Post("산책의뢰", "우리동네 좋은 동물병원 추천", "24시간 응급실 있는 곳으로 알려드려요", "인의동 · 1일 전 · 조회 42", 12, R.drawable.pp_logo),
        Post("돌봄의뢰", "우리 댕댕이 첫 산책!", "생후 3개월 처음으로 밖에 나가봤어요", "인의동 · 2일 전 · 조회 67", 18, R.drawable.pp_logo),
        Post("돌봄구인", "강아지 사료 공동구매 하실분", "대용량으로 사면 더 저렴해요!", "인의동 · 3일 전 · 조회 31", 9, R.drawable.pp_logo)
    )

    /* 더미 피드 데이터 */
    private val _allFeeds = MutableStateFlow(dummyFeeds)
    private val _filteredFeeds = MutableStateFlow(dummyFeeds)
    val filteredFeeds: StateFlow<List<FeedDto>> = _filteredFeeds

    private val _filteredPosts = MutableStateFlow(_allPosts)
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts


    val allCategories = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    /* 하나만 선택(재클릭 → 해제) */
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

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
        val cat = _selectedCategory.value
        val query = _searchText.value.lowercase()

        _filteredFeeds.value = _allFeeds.value.filter { feed ->
            (cat == null || feed.category == cat) &&
                    (query.isBlank() || feed.content.lowercase().contains(query))
        }

        _filteredPosts.value = _allPosts.filter { post ->
            (cat == null || post.category == cat) &&
                    (query.isBlank() || post.title.lowercase().contains(query) || post.title.lowercase().contains(query))
        }
    }
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
private val tempImg = listOf(
    ImgDto("https://lh4.googleusercontent.com/proxy/d9kCctaZDANtXrlzOCIfN9dV8y0d0wD75pIdJ7RVeebztPErjpoy-oskh3PGWrm8jHuDDhNjMCzzD4PJ1RPFF4HRZckQcCEQfxyMWPQ-",1),
    ImgDto("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Felis_catus-cat_on_snow.jpg/640px-Felis_catus-cat_on_snow.jpg",2)
)

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
        images = tempImg
    ),
    FeedDto(
        id = 4, content = "오늘 초코가 새로운 장난감을 좋아했어요!",
        userId = userChulsoo.first, userNick = userChulsoo.second, userImg = userChulsoo.third,
        regionId = 1001, category = "INFO", createdAt = TS,
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
        regionId = 1003, category = "나눔", createdAt = TS,
        tags = listOf(tagRabbit, tagGoods)
    ),
    FeedDto(
        id = 26, content = "토토가 너무 귀여워서 사진 찍었어요 📸",
        userId = userMinsu.first, userNick = userMinsu.second, userImg = userMinsu.third,
        regionId = 1003, category = "공구", createdAt = TS,
        tags = listOf(tagRabbit, tagReview)
    ),
    FeedDto(
        id = 28, content = "오늘 토토가 제 손을 핥았어요 🥰",
        userId = userMinsu.first, userNick = userMinsu.second, userImg = userMinsu.third,
        regionId = 1003, category = "자유", createdAt = TS,
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

