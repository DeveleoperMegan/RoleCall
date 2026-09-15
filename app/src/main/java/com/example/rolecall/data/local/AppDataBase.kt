package com.example.rolecall.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.local.entity.MatchHistoryEntity
import com.example.rolecall.data.local.entity.ResumeEntity
import com.example.rolecall.data.local.entity.SavedJobEntity
import com.example.rolecall.data.local.entity.GeneratedResumeEntity
import com.example.rolecall.data.local.dao.GeneratedResumeDao

@Database(
    entities = [
        SavedJobEntity::class,
        ResumeEntity::class,
        MatchHistoryEntity::class,
        GeneratedResumeEntity::class       //NEW
    ],
    version = 5,                            //bumped from 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedJobDao(): SavedJobDao
    abstract fun resumeDao(): ResumeDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun generatedResumeDao(): GeneratedResumeDao   //NEW
}