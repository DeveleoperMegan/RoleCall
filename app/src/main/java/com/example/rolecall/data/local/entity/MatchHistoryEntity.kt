package com.example.rolecall.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resumeId: Long,
    val jobId: String,
    val score: Double,
    val date: Long = System.currentTimeMillis()
)
