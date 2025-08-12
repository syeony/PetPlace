// presentation/feature/feed/BoardWriteViewModel.kt
package com.example.petplace.presentation.feature.feed

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.feed.CreateImage
import com.example.petplace.data.model.feed.FeedCreateReq
import com.example.petplace.data.model.feed.FeedCreateRes
import com.example.petplace.data.repository.FeedRepository
import com.example.petplace.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BoardWriteViewModel @Inject constructor(
    private val feedrepo: FeedRepository,
    private val imageRepo: ImageRepository,
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

    // 이미지관련 변수
    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris
    fun setImageUris(list: List<Uri>) {
        _imageUris.value = list
    }

    fun updateContent(t:String) { _content.value = t }
    fun pickCategory(c:String) { _category.value = c }
    fun toggleTag(id:Long) {
        _tagIds.update { if (id in it) it - id else if (it.size < 4) it + id else it }
    }
    fun setImages(list: List<CreateImage>) { _images.value = list }

    // 이미지 업로드 및 피드 등록
    suspend fun uploadImagesAndSubmitFeed(): FeedCreateRes {
        // 1. 이미지 업로드
        val urls = if (_imageUris.value.isNotEmpty()) {
            imageRepo.uploadImages(_imageUris.value)
        } else emptyList()
        Log.d("UploadURLs", "업로드된 이미지 URL 목록: $urls")

        // 2. CreateImage 모델로 변환
        val images = urls.mapIndexed { idx, url ->
            CreateImage(src = url, sort = idx)
        }

        // 3. Feed 등록
        val app = context as PetPlaceApp
        val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")
        val body = FeedCreateReq(
            content = _content.value.trim(),
            regionId = user.regionId,
            category = _category.value ?: "ANY",
            tagIds = _tagIds.value,
            images = images
        )
        return feedrepo.createFeed(body)
    }

    // BoardWriteViewModel.kt
    fun removeImageUri(uri: Uri) {
        _imageUris.value = _imageUris.value - uri
        setImages(_imageUris.value.mapIndexed { index, u ->
            CreateImage(src = u.toString(), sort = index)
        })
    }

}

