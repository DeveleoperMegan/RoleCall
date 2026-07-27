package com.example.rolecall.data.remote

import com.google.gson.annotations.SerializedName

data class ResumeItem(
    val id: String,
    val filename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("created_at") val createdAt: String
)

