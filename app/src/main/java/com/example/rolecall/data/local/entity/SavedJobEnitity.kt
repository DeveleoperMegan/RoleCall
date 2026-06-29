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
    val status: String = "saved",     // "saved", "applied", "interviewing", "offer"
    val dateSaved: Long = System.currentTimeMillis()
)
