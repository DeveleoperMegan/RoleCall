package com.example.rolecall.data.local.dao

import androidx.room.*
import com.example.rolecall.data.local.entity.GeneratedResumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedResumeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resume: GeneratedResumeEntity)

    @Delete
    suspend fun delete(resume: GeneratedResumeEntity)

    @Query("SELECT * FROM generated_resumes ORDER BY dateGenerated DESC")
    fun getAll(): Flow<List<GeneratedResumeEntity>>

    @Query("SELECT * FROM generated_resumes WHERE id = :id")
    suspend fun getById(id: String): GeneratedResumeEntity?

    @Query("UPDATE generated_resumes SET filename = :newName WHERE id = :id")
    suspend fun updateFilename(id: String, newName: String)
}
