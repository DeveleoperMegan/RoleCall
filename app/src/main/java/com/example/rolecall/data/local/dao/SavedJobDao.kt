package com.example.rolecall.data.local.dao
import androidx.room.*
import com.example.rolecall.data.local.entity.SavedJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveJob(job: SavedJobEntity)

    @Delete
    suspend fun deleteJob(job: SavedJobEntity)

    @Query("SELECT * FROM saved_jobs ORDER BY dateSaved DESC")
    fun getAllSavedJobs(): Flow<List<SavedJobEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE jobId = :jobId)")
    fun isJobSaved(jobId: String): Flow<Boolean>
}
