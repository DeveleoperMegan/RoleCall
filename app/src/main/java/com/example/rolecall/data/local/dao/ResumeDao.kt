package com.example.rolecall.data.local.dao
import androidx.room.*
import com.example.rolecall.data.local.entity.ResumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {

    @Insert
    suspend fun insertResume(resume: ResumeEntity): Long

    @Update
    suspend fun updateResume(resume: ResumeEntity)

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    @Query("SELECT * FROM resumes ORDER BY dateAdded DESC")
    fun getAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :resumeId")
    suspend fun getResumeById(resumeId: Long): ResumeEntity?
}