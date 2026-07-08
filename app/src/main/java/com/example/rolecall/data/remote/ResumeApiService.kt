package com.example.rolecall.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val extractedText: String? = null,
    val filename: String? = null
)

data class MatchRequest(
    val resume_text: String
)

data class MatchResponse(
    val matches: List<MatchResult>
)

data class MatchResult(
    val jobId: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val score: Double,
    val matchingPhrases: List<MatchingPhrase>? = null,
    val keySkills: List<String>? = null
)

data class MatchingPhrase(
    val text: String,
    val weight: Double
)

interface ResumeApiService {

    @Multipart
    @POST("/upload/resume")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("/match")
    suspend fun matchResume(
        @Body request: MatchRequest
    ): Response<MatchResponse>
}