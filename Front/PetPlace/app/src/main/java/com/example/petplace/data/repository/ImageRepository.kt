package com.example.petplace.data.repository

import android.content.Context
import android.net.Uri
import com.example.petplace.data.remote.ImageApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val api: ImageApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun uploadImages(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val parts = uris.mapIndexed { idx, uri ->
            val file = uriToFile(context, uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.name, requestFile)
        }
        api.uploadImages(parts).urls
    }
    // Uri -> File 변환
    private fun uriToFile(context: Context, uri: Uri): File {
        // 임시 파일로 복사 (아래 유틸 참고)
        val input = context.contentResolver.openInputStream(uri)!!
        val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
        file.outputStream().use { output -> input.copyTo(output) }
        return file
    }
}
