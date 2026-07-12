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
            description = "We are seeking a skilled Android Developer to join our growing team. The ideal candidate has a strong background in mobile development and a passion for building intuitive user experiences.",
            matchScore = 92f,
            minSalary = 90000.0,
            maxSalary = 130000.0,
            postDate = "2025-06-15",
            postUrl = "https://example.com/jobs/1"
        ),
        JobItem(
            id = "2",
            title = "Backend Engineer",
            company = "StartupXYZ",
            location = "New York, NY",
            description = "Join our backend team and build scalable APIs that power millions of requests per day.",
            matchScore = 85f,
            minSalary = 110000.0,
            maxSalary = 160000.0,
            postDate = "2025-06-20",
            postUrl = "https://example.com/jobs/2"
        ),
        JobItem(
            id = "3",
            title = "Data Scientist",
            company = "DataGenius",
            location = "San Francisco, CA",
            description = "Analyze complex datasets and build machine learning models to drive business decisions.",
            matchScore = 78f,
            minSalary = 120000.0,
            maxSalary = 170000.0,
            postDate = "2025-06-10",
            postUrl = "https://example.com/jobs/3"
        ),
        JobItem(
            id = "4",
            title = "UX Designer",
            company = "DesignLab",
            location = "Austin, TX",
            description = "Create beautiful and intuitive user experiences for our suite of products.",
            matchScore = 64f,
            minSalary = 80000.0,
            maxSalary = 120000.0,
            postDate = "2025-06-18",
            postUrl = "https://example.com/jobs/4"
        ),
        JobItem(
            id = "5",
            title = "Product Manager",
            company = "Innovate Inc.",
            location = "Seattle, WA",
            description = "Lead product development from ideation to launch, working closely with engineering and design teams.",
            matchScore = 51f,
            minSalary = 100000.0,
            maxSalary = 150000.0,
            postDate = "2025-06-22",
            postUrl = "https://example.com/jobs/5"
        )
    )
}
