package com.example.rolecall.data.mock

import com.example.rolecall.data.model.JobItem

/**
 * Provides static mock job match data for development and testing.
 */
object MockData {

    /**
     * Returns a list of mock job matches.
     */
    fun getMockJobs(): List<JobItem> = listOf(
        JobItem(
            id = "1",
            title = "Android Developer",
            company = "TechCorp",
            location = "Remote",
            matchScore = 92f
        ),
        JobItem(
            id = "2",
            title = "Backend Engineer",
            company = "StartupXYZ",
            location = "New York, NY",
            matchScore = 85f
        ),
        JobItem(
            id = "3",
            title = "Data Scientist",
            company = "DataGenius",
            location = "San Francisco, CA",
            matchScore = 78f
        ),
        JobItem(
            id = "4",
            title = "UX Designer",
            company = "DesignLab",
            location = "Austin, TX",
            matchScore = 64f
        ),
        JobItem(
            id = "5",
            title = "Product Manager",
            company = "Innovate Inc.",
            location = "Seattle, WA",
            matchScore = 51f
        )
    )
}
