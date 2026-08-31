package com.example.rolecall.data.repository

import com.example.rolecall.data.local.dao.MatchHistoryDao
import com.example.rolecall.data.local.dao.ResumeDao
import com.example.rolecall.data.local.dao.SavedJobDao
import com.example.rolecall.data.local.entity.MatchHistoryEntity
import com.example.rolecall.data.local.entity.ResumeEntity
import com.example.rolecall.data.local.entity.SavedJobEntity
import com.example.rolecall.data.model.JobItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JobRepository(
    private val savedJobDao: SavedJobDao,
    private val resumeDao: ResumeDao,
    private val matchHistoryDao: MatchHistoryDao
) {

    // ── Saved / applied jobs ────────────────────────────────────────────────

    fun getAllSavedJobs(): Flow<List<JobItem>> {
        return savedJobDao.getAllSavedJobs().map { entities ->
            entities.map { it.toJobItem() }
        }
    }

    fun getAppliedJobs(): Flow<List<JobItem>> {
        return savedJobDao.getAppliedJobs().map { entities ->
            entities.map { it.toJobItem() }
        }
    }

    fun getJobsByStatus(status: String): Flow<List<JobItem>> {
        return savedJobDao.getJobsByStatus(status).map { entities ->
            entities.map { it.toJobItem() }
        }
    }

    fun getAllJobs(): Flow<List<JobItem>> {
        return savedJobDao.getAllJobs().map { entities ->
            entities.map { it.toJobItem() }
        }
    }

    fun isJobSaved(jobId: String): Flow<Boolean> {
        return savedJobDao.isJobSaved(jobId)
    }

    suspend fun saveJob(job: JobItem) {
        savedJobDao.saveJob(job.toEntity())
    }

    suspend fun deleteSavedJob(job: JobItem) {
        savedJobDao.deleteJob(job.toEntity())
    }

    suspend fun applyToJob(job: JobItem) {
        savedJobDao.updateJobStatus(job.id, "applied")
    }

    suspend fun updateJobStatus(jobId: String, status: String) {
        savedJobDao.updateJobStatus(jobId, status)
    }

    // ── Resumes ─────────────────────────────────────────────────────────────

    suspend fun saveResume(name: String, rawText: String): Long {
        return resumeDao.insertResume(ResumeEntity(name = name, rawText = rawText))
    }

    suspend fun deleteResume(resume: ResumeEntity) {
        resumeDao.deleteResume(resume)
    }

    fun getAllResumes(): Flow<List<ResumeEntity>> {
        return resumeDao.getAllResumes()
    }

    // ── Match history ───────────────────────────────────────────────────────

    suspend fun recordMatch(resumeId: Long, jobId: String, score: Double) {
        matchHistoryDao.insertMatch(
            MatchHistoryEntity(resumeId = resumeId, jobId = jobId, score = score)
        )
    }

    fun getMatchHistoryForResume(resumeId: Long): Flow<List<MatchHistoryEntity>> {
        return matchHistoryDao.getMatchHistoryForResume(resumeId)
    }

    fun getAllMatchHistory(): Flow<List<MatchHistoryEntity>> {
        return matchHistoryDao.getAllMatchHistory()
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private fun JobItem.toEntity() = SavedJobEntity(
        jobId = id,
        title = title,
        company = company,
        location = location,
        description = description,
        matchScore = matchScore,
        status = status,
        dateSaved = System.currentTimeMillis(),
        maxSalary = maxSalary,
        minSalary = minSalary,
        postDate = postDate,
        postUrl = postUrl
    )

    private fun SavedJobEntity.toJobItem() = JobItem(
        id = jobId,
        title = title,
        company = company,
        location = location,
        description = description,
        matchScore = matchScore,
        maxSalary = maxSalary,
        minSalary = minSalary,
        postDate = postDate,
        postUrl = postUrl,
        status = status
    )
}