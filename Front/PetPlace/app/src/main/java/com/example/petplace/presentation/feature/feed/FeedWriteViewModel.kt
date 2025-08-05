// presentation/feature/feed/BoardWriteViewModel.kt
package com.example.petplace.presentation.feature.feed

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.feed.CreateImage
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedCreateRes
import com.example.petplace.data.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BoardWriteViewModel @Inject constructor(
    private val repo: FeedRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _content   = MutableStateFlow("")
    private val _category  = MutableStateFlow<String?>(null)
    private val _tagIds    = MutableStateFlow<List<Long>>(emptyList())
    private val _images    = MutableStateFlow<List<CreateImage>>(emptyList())

    val content  : StateFlow<String>            = _content
    val category : StateFlow<String?>           = _category
    val tagIds   : StateFlow<List<Long>>        = _tagIds
    val images   : StateFlow<List<CreateImage>> = _images

    fun updateContent(t:String) { _content.value = t }
    fun pickCategory(c:String) { _category.value = c }
    fun toggleTag(id:Long) {
        _tagIds.update { if (id in it) it - id else if (it.size < 4) it + id else it }
    }
    fun setImages(list: List<CreateImage>) { _images.value = list }

    suspend fun submit(): FeedCreateRes {
        val app = context as PetPlaceApp
        val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

        val body = FeedCreateReq(
            content = _content.value.trim(),
            regionId = user.regionId,
            category = _category.value ?: "MYPET",
            tagIds = _tagIds.value,
            images = _images.value
        )
        return repo.createFeed(body)
    }
}

