package com.example.rolecall.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val extractedText: String? = null,
    val filename: String? = null
)

interface ResumeApiService {

    @Multipart
    @POST("/upload/resume")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}