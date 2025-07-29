package com.example.petplace.presentation.feature.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.presentation.feature.board.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardViewModel : ViewModel() {
    val allCategories = listOf("내새꾸자랑", "나눔", "공구", "정보", "자유")

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _allPosts = MutableStateFlow(samplePosts)
    private val _filteredPosts = MutableStateFlow(samplePosts)
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    init {
        applyFilters()
    }

    fun toggleCategory(category: String) {
        _selectedCategories.update { current ->
            if (current.contains(category)) current - category else current + category
        }
        applyFilters()
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val categories = _selectedCategories.value
            val query = _searchText.value.lowercase()

            _filteredPosts.value = _allPosts.value.filter { post ->
                (categories.isEmpty() || categories.contains(post.category)) &&
                        (query.isBlank() || post.content.lowercase().contains(query))
            }
        }
    }
}

val samplePosts = listOf(
    Post(
        profileImage = "https://randomuser.me/api/portraits/women/1.jpg",
        category = "내새꾸자랑",
        author = "이도형",
        content = "오늘 처음으로 집에서 목욕시켜봤는데 생각보다 순했어요! 처음엔 무서워했지만 금세 적응하더라구요 ㅎㅎ",
        hashtags = listOf("#골든리트리버", "#목욕", "#첫경험", "#귀여워"),
        imageUrl = "https://lh4.googleusercontent.com/proxy/d9kCctaZDANtXrlzOCIfN9dV8y0d0wD75pIdJ7RVeebztPErjpoy-oskh3PGWrm8jHuDDhNjMCzzD4PJ1RPFF4HRZckQcCEQfxyMWPQ-",
        location = "인의동",
        likes = 24,
        comments = 8
    ),
    Post(
        profileImage = "https://randomuser.me/api/portraits/men/2.jpg",
        category = "나눔",
        author = "송정현",
        content = "집에 쌓여있는 고양이 장난감들 나눔합니다! 우리 냥이가 안 가지고 놀아서... 필요하신 분 댓글 남겨주세요",
        hashtags = listOf("#고양이", "#장난감", "#나눔", "#무료"),
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Felis_catus-cat_on_snow.jpg/640px-Felis_catus-cat_on_snow.jpg",
        location = "인의동",
        likes = 12,
        comments = 15
    ),
    Post(
        profileImage = "https://randomuser.me/api/portraits/women/3.jpg",
        category = "내새꾸자랑",
        author = "정유진",
        content = "오늘도 열심히 해바라기씨 까먹는 우리 햄찌 ㅋㅋ 볼주머니 가득 채우고 뿌듯한 표정이에요",
        hashtags = listOf("#햄스터", "#간식", "#cute", "#해바라기씨"),
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/PhodopusSungorus_2.jpg/640px-PhodopusSungorus_2.jpg",
        location = "인의동",
        likes = 31,
        comments = 6
    )
)
