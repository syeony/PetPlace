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

val samplePosts = listOf(
    Post(
        id = "1",
        profileImage = "https://randomuser.me/api/portraits/women/1.jpg",
        category = "내새꾸자랑",
        author = "이도형",
        content = "오늘 처음으로 집에서 목욕시켜봤는데 생각보다 순했어요! 처음엔 무서워했지만 금세 적응하더라구요 ㅎㅎ",
        hashtags = listOf("#골든리트리버", "#목욕", "#첫경험", "#귀여워"),
        imageUrls = listOf(
            "https://lh4.googleusercontent.com/proxy/d9kCctaZDANtXrlzOCIfN9dV8y0d0wD75pIdJ7RVeebztPErjpoy-oskh3PGWrm8jHuDDhNjMCzzD4PJ1RPFF4HRZckQcCEQfxyMWPQ-",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Felis_catus-cat_on_snow.jpg/640px-Felis_catus-cat_on_snow.jpg"
        ),
        location = "인의동",
        likes = 24,
        comments = 8
    ),
    Post(
        id = "2",
        profileImage = "https://randomuser.me/api/portraits/men/2.jpg",
        category = "나눔",
        author = "송정현",
        content = "집에 쌓여있는 고양이 장난감들 나눔합니다! 우리 냥이가 안 가지고 놀아서... 필요하신 분 댓글 남겨주세요",
        hashtags = listOf("#고양이", "#장난감", "#나눔", "#무료"),
        imageUrls = listOf(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Felis_catus-cat_on_snow.jpg/640px-Felis_catus-cat_on_snow.jpg",
        ),
        location = "인의동",
        likes = 12,
        comments = 15
    ),
    Post(
        id = "3",
        profileImage = "https://randomuser.me/api/portraits/women/3.jpg",
        category = "내새꾸자랑",
        author = "정유진",
        content = "오늘도 열심히 해바라기씨 까먹는 우리 햄찌 ㅋㅋ 볼주머니 가득 채우고 뿌듯한 표정이에요",
        hashtags = listOf("#햄스터", "#간식", "#cute", "#해바라기씨"),
        imageUrls = listOf(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/PhodopusSungorus_2.jpg/640px-PhodopusSungorus_2.jpg",
            ),
        location = "인의동",
        likes = 31,
        comments = 6
    )
)
val sampleComments = listOf(
    Comment(
        postId = "1",
        author = "김지은",
        profileImage = "https://randomuser.me/api/portraits/women/10.jpg",
        town = "인의동",
        text = "너무 귀엽네요!",
        isMine = false,
        replies = listOf(
            Reply(
                author = "나",
                profileImage = "https://randomuser.me/api/portraits/men/5.jpg",
                town = "인의동",
                text = "감사합니다! 😆",
                isMine = true
            ),
            Reply(
                author = "박민수",
                profileImage = "https://randomuser.me/api/portraits/men/12.jpg",
                town = "인의동",
                text = "저도 귀엽다고 생각해요!",
                isMine = false
            )
        )
    ),
    Comment(
        postId = "1",
        author = "나",
        profileImage = "https://randomuser.me/api/portraits/men/5.jpg",
        town = "인의동",
        text = "감사해요!",
        isMine = true
    ),
    Comment(
        postId = "1",
        author = "이수현",
        profileImage = "https://randomuser.me/api/portraits/women/20.jpg",
        town = "인의동",
        text = "강아지 종이 뭐에요?",
        isMine = false,
        replies = listOf(
            Reply(
                author = "나",
                profileImage = "https://randomuser.me/api/portraits/men/5.jpg",
                town = "인의동",
                text = "말티즈에요!",
                isMine = true
            )
        )
    ),
    Comment(
        postId = "1",
        author = "최유진",
        profileImage = "https://randomuser.me/api/portraits/women/30.jpg",
        town = "인의동",
        text = "저희 집 강아지도 친구하고 싶어할 듯! 🐶",
        isMine = false
    ),
    Comment(
        postId = "1",
        author = "박성민",
        profileImage = "https://randomuser.me/api/portraits/men/15.jpg",
        town = "인의동",
        text = "귀엽네요 ㅎㅎ",
        isMine = false
    )
)
