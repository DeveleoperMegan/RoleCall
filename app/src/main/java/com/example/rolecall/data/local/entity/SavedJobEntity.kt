package com.example.rolecall.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_jobs")
data class SavedJobEntity(
    @PrimaryKey val jobId: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val matchScore: Float,
    val status: String = "saved",
    val dateSaved: Long = System.currentTimeMillis(),
    val maxSalary: Double? = null,
    val minSalary: Double? = null,
    val postDate: String? = null,
    val postUrl: String? = null
)
