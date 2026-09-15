package com.example.rolecall.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_resumes")
data class GeneratedResumeEntity(
    @PrimaryKey val id: String,          // resume_id from the generation backend
    val filename: String,
    val template: String,
    val localPath: String,               // path in filesDir
    val dateGenerated: Long = System.currentTimeMillis()
)