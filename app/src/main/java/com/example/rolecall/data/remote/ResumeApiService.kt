package com.example.rolecall.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class UploadResponse(
    val resume_id: String,
    val filename: String,
    val text_length: Int
)

data class JobPostingResult(
    val id: String,
    val job_id: Int?,
    val company_name: String?,
    val title: String,
    val description: String,
    val max_salary: Double?,
    val min_salary: Double?,
    val post_date: String?,
    val post_url: String?,
    val expiration_date: String?,
    val similarity: Double
)

data class SearchResponse(
    val resume_id: String,
    val matches: List<JobPostingResult>
)

data class JobPostingsPage(
    val items: List<JobPostingResult>,
    val next_page_index: String?,
    val has_next: Boolean
)

interface ResumeApiService {

    @Multipart
    @POST("/api/v1/resumes/upload")
    suspend fun uploadResume(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("/api/v1/search/job_postings")
    suspend fun searchJobs(
        @Query("resume_id") resumeId: String,
        @Query("after") after: String? = null
    ): Response<SearchResponse>

    @GET("/api/v1/search/job_postings")
    suspend fun browseJobs(
        @Query("after") after: String? = null
    ): Response<JobPostingsPage>
}