package com.example.rolecall.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rawText: String,
    val dateAdded: Long = System.currentTimeMillis()
)
