package com.example.rolecall.data.model

/**
 * Represents a job match result displayed on the results screen.
 *
 * @param id A unique identifier for the job.
 * @param title The job title.
 * @param company The hiring company.
 * @param location The job location (city/remote).
 * @param matchScore The cosine similarity score (0‑100%) between the résumé and this job.
 */
data class JobItem(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val matchScore: Float
)