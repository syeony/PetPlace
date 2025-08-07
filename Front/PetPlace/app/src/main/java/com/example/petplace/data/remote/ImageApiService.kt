package com.example.petplace.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApiService {
    @Multipart
    @POST("/api/upload/images")
    suspend fun uploadImages(
        @Part files: List<MultipartBody.Part>
    ): ImageUploadResponse
}

data class ImageUploadResponse(
    val urls: List<String>
)
