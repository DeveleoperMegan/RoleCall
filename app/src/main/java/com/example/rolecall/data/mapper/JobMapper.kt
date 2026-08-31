package com.example.rolecall.data.mapper

import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.remote.MatchJsonItem
import com.example.rolecall.data.remote.SearchJsonResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object JobMapper {

    private val gson = Gson()

    /**
     * Parses a raw search JSON string and converts the matches
     * into a list of [JobItem] domain models.
     *
     * Returns an empty list if the JSON is invalid or has no matches.
     */
    fun parseSearchResponseToJobs(json: String): List<JobItem> {
        return try {
            val response = gson.fromJson(json, SearchJsonResponse::class.java)
            fromMatchItems(response.matches)
        } catch (e: JsonSyntaxException) {
            emptyList()
        }
    }

    /**
     * Converts a list of [MatchJsonItem] into a list of [JobItem].
     * Handles nullability and maps similarity to a 0–100 float.
     *
     * TEMPORARY: Adds mock matchingPhrases and keySkills when the backend
     * does not provide them. Remove the fallback lines when real data is available.
     */
    fun fromMatchItems(matches: List<MatchJsonItem>?): List<JobItem> {
        return matches?.map { match ->
            // Mock fallbacks for testing (remove once backend provides real fields)
            val mockPhrases = listOf(
                "Strong communication skills",
                "Experience with relevant technologies",
                "Proven ability to work in a team"
            )
            val mockSkills = listOf(
                "Communication",
                "Problem Solving",
                "Team Collaboration"
            )

            JobItem(
                id = match.id,
                title = match.title,
                company = match.companyName ?: "Unknown",
                location = "",
                description = match.description,
                matchScore = (match.similarity * 100).toFloat(),
                maxSalary = match.maxSalary,
                minSalary = match.minSalary,
                postDate = match.postDate,
                postUrl = match.postUrl,
                matchingPhrases = match.matchingPhrases ?: mockPhrases,   // fallback
                keySkills = match.keySkills ?: mockSkills                // fallback
            )
        } ?: emptyList()
    }
}