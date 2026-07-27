package com.example.rolecall.data.remote

import com.google.gson.annotations.SerializedName

data class ResumeDetail(
    val id: String,
    val filename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("raw_text") val rawText: String?,
    @SerializedName("created_at") val createdAt: String
)