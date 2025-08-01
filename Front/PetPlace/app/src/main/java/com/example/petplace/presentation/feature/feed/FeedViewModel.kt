package com.example.petplace.presentation.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.feed.Comment
import com.example.petplace.data.local.feed.Post
import com.example.petplace.data.local.feed.Reply
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BoardViewModel : ViewModel() {
    val allCategories = listOf("내새꾸자랑", "나눔", "공구", "정보", "자유")

//    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
//    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    /* ───────── 카테고리 선택을 “하나”만 보유 ───────── */
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _allPosts = MutableStateFlow(samplePosts)
    private val _filteredPosts = MutableStateFlow(samplePosts)
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    private val _comments = MutableStateFlow(sampleComments)

    init {
        applyFilters()
    }

    /* ---------- 카테고리 토글 ---------- */
    fun toggleCategory(category: String) {
        _selectedCategory.value =
            if (_selectedCategory.value == category) null     // 같은 버튼 → 해제
            else category                                     // 다른 버튼 → 교체

        applyFilters()
    }

    /* ---------- 검색어 업데이트 ---------- */
    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }

    /* ---------- 필터링 ---------- */
    private fun applyFilters() {
        viewModelScope.launch {
            val picked = _selectedCategory.value
            val query  = _searchText.value.lowercase()

            _filteredPosts.value = _allPosts.value.filter { post ->
                (picked == null || post.category == picked) &&
                        (query.isBlank() || post.content.lowercase().contains(query))
            }
        }
    }

    /* ---------- 댓글 ---------- */
    fun getCommentsForPost(postId: String): List<Comment> =
        _comments.value.filter { it.postId == postId }
}

/* ---------- 더미 데이터 ---------- */
val samplePosts = listOf(
    /* ① 초코 산책 (댓글 3 개) */
    Post(
        id         = "1",
        profileImage = "",                        // 프로필 이미지 없으면 빈 문자열
        category   = "내새꾸자랑",                // "MYPET" → 화면용 카테고리
        author     = "철수",
        content    = "우리 초코 산책 다녀왔어요! 너무 귀엽네요 🐶",
        hashtags   = listOf("#강아지", "#산책코스", "#간식추천"),
        imageUrls  = listOf("https://picsum.photos/id/1011/800/600"),  // 샘플 사진
        location   = "인의동",                    // regionId 1001
        likes      = 3,
        comments   = 3
    ),

    /* ② 초코 새 장난감 */
    Post(
        id = "4",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "철수",
        content    = "오늘 초코가 새로운 장난감을 좋아했어요!",
        hashtags   = listOf("#강아지", "#귀여움주의"),
        imageUrls  = listOf("https://picsum.photos/id/1025/800/600"),
        location   = "인의동",
        likes      = 0,
        comments   = 0
    ),

    /* ③ 초코 첫 친구 */
    Post(
        id = "7",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "철수",
        content    = "초코가 처음으로 친구 강아지를 만났어요 🐕",
        hashtags   = listOf("#강아지", "#사진공유"),
        imageUrls  = listOf("https://picsum.photos/id/237/800/600"),
        location   = "인의동",
        likes      = 0,
        comments   = 0
    ),

    /* ④ 토토 첫 산책 */
    Post(
        id = "21",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "민수",
        content    = "토토가 오늘 첫 산책을 나갔어요 🐇",
        hashtags   = listOf("#토끼", "#반려동물용품"),
        imageUrls  = listOf("https://picsum.photos/id/433/800/600"),
        location   = "봉곡동",                    // regionId 1003
        likes      = 0,
        comments   = 0
    ),

    /* ⑤ 토토 사진 */
    Post(
        id = "26",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "민수",
        content    = "토토가 너무 귀여워서 사진 찍었어요 📸",
        hashtags   = listOf("#토끼", "#입양후기"),
        imageUrls  = listOf("https://picsum.photos/id/582/800/600"),
        location   = "봉곡동",
        likes      = 0,
        comments   = 0
    ),

    /* ⑥ 토토 핥핥 */
    Post(
        id = "28",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "민수",
        content    = "오늘 토토가 제 손을 핥았어요 🥰",
        hashtags   = listOf("#토끼", "#사진공유"),
        imageUrls  = listOf("https://picsum.photos/id/593/800/600"),
        location   = "봉곡동",
        likes      = 0,
        comments   = 0
    ),

    /* ⑦ 나비 창밖 구경 */
    Post(
        id = "11",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "영희",
        content    = "우리 나비가 창밖을 보며 하루종일 앉아있네요 🐱",
        hashtags   = listOf("#고양이", "#귀여움주의"),
        imageUrls  = listOf("https://picsum.photos/id/1032/800/600"),
        location   = "원평동",                   // regionId 1002
        likes      = 0,
        comments   = 0
    ),

    /* ⑧ 캣타워 Zzz */
    Post(
        id = "14",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "영희",
        content    = "나비가 캣타워에서 놀다가 자버렸어요 😺",
        hashtags   = listOf("#고양이", "#건강관리", "#사료후기"),
        imageUrls  = listOf("https://picsum.photos/id/1024/800/600"),
        location   = "원평동",
        likes      = 0,
        comments   = 0
    ),

    /* ⑨ 간식 첫 경험 */
    Post(
        id = "17",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "영희",
        content    = "우리 집 냥이가 처음으로 간식 먹어봤어요!",
        hashtags   = listOf("#고양이", "#반려동물용품"),
        imageUrls  = listOf("https://picsum.photos/id/1074/800/600"),
        location   = "원평동",
        likes      = 0,
        comments   = 0
    ),

    /* ⑩ 오늘도 귀여운 나비 */
    Post(
        id = "19",
        profileImage = "",
        category   = "내새꾸자랑",
        author     = "영희",
        content    = "나비가 오늘 너무 귀엽네요 💕",
        hashtags   = listOf("#고양이", "#귀여움주의"),
        imageUrls  = listOf("https://picsum.photos/id/1084/800/600"),
        location   = "원평동",
        likes      = 0,
        comments   = 0
    )
)

/* 댓글 (피드 #1 전용) ---------------------------------------------------- */
val sampleComments = listOf(
    Comment(
        postId       = "1",
        author       = "영희",
        profileImage = "",
        town         = "인의동",
        text         = "초코 너무 귀여워요 🐶",
        isMine       = false,
        replies      = listOf(
            Reply(
                author       = "철수",
                profileImage = "",
                town         = "인의동",
                text         = "감사해요! 한강공원에서 했어요!",
                isMine       = true
            )
        )
    ),
    Comment(
        postId       = "1",
        author       = "민수",
        profileImage = "",
        town         = "인의동",
        text         = "산책 어디서 하셨나요?",
        isMine       = false
    )
)