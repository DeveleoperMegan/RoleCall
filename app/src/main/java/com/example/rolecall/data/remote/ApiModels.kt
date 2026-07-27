package com.example.rolecall.data.remote

import com.google.gson.annotations.SerializedName

// ── Upload response ──────────────────────────────────────────────────────────
data class UploadJsonResponse(
    @SerializedName("resume_id") val resumeId: String,
    val filename: String,
    @SerializedName("text_length") val textLength: Int
)

// ── Search response ──────────────────────────────────────────────────────────
data class SearchJsonResponse(
    @SerializedName("resume_id") val resumeId: String?,
    @SerializedName("matches") val matches: List<MatchJsonItem>?
)

data class MatchJsonItem(
    val id: String,
    @SerializedName("job_id") val jobId: Long?,
    @SerializedName("company_name") val companyName: String?,
    val title: String,
    val description: String,
    @SerializedName("max_salary") val maxSalary: Double?,
    @SerializedName("min_salary") val minSalary: Double?,
    @SerializedName("post_date") val postDate: String?,
    @SerializedName("post_url") val postUrl: String?,
    @SerializedName("expiration_date") val expirationDate: String?,
    val similarity: Double
)

// ── Resume list item (already existed, keep it) ──────────────────────────────
data class ResumeItem(
    val id: String,
    val filename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("created_at") val createdAt: String
)

// ── Resume detail (for preview) ──────────────────────────────────────────────
data class ResumeDetail(
    val id: String,
    val filename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("raw_text") val rawText: String?,
    @SerializedName("created_at") val createdAt: String
)

