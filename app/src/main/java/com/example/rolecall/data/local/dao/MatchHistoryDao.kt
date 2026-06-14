package com.example.rolecall.data.local.dao
import androidx.room.*
import com.example.rolecall.data.local.entity.MatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchHistoryDao {

    @Insert
    suspend fun insertMatch(match: MatchHistoryEntity)

    @Query("SELECT * FROM match_history ORDER BY date DESC")
    fun getAllMatchHistory(): Flow<List<MatchHistoryEntity>>

    @Query("SELECT * FROM match_history WHERE resumeId = :resumeId ORDER BY date DESC")
    fun getMatchHistoryForResume(resumeId: Long): Flow<List<MatchHistoryEntity>>

    @Delete
    suspend fun deleteMatch(match: MatchHistoryEntity)
}