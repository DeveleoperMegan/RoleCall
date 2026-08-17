package com.example.rolecall.data.model

/**
 * Represents a job match result displayed on the results screen.
 *
 * @param id A unique identifier for the job.
 * @param title The job title.
 * @param company The hiring company.
 * @param location The job location (city/remote).
 * @param description The full job description text.
 * @param matchScore The similarity score (0–100%) between the résumé and this job.
 * @param maxSalary The maximum salary for the position, if available.
 * @param minSalary The minimum salary for the position, if available.
 * @param postDate The date the job was posted, in ISO 8601 format, if available.
 * @param postUrl The original URL of the job posting, if available.
 */

data class JobItem(
    val id: String,
    val title: String,
    val company: String,
    val location: String = "",
    val description: String = "",
    val matchScore: Float,
    val maxSalary: Double? = null,
    val minSalary: Double? = null,
    val postDate: String? = null,
    val postUrl: String? = null,
    // NEW fields
    val matchingPhrases: List<String> = emptyList(),
    val keySkills: List<String> = emptyList()
)