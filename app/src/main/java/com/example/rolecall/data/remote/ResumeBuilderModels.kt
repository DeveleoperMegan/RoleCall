package com.example.rolecall.data.remote

import com.google.gson.annotations.SerializedName

data class ResumeGenerationRequest(
    @SerializedName("contact") val contact: ContactInfo,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("work_experience") val workExperience: List<WorkExperience>,
    @SerializedName("education") val education: List<Education>,
    @SerializedName("skills") val skills: List<String>,
    @SerializedName("projects") val projects: List<Project>? = null,
    @SerializedName("certifications") val certifications: List<String>? = null,
    @SerializedName("template") val template: String = "classic"
)

data class ContactInfo(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("linkedin") val linkedin: String? = null,
    @SerializedName("portfolio") val portfolio: String? = null,
    @SerializedName("github") val github: String? = null
)

data class WorkExperience(
    @SerializedName("company") val company: String,
    @SerializedName("job_title") val jobTitle: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("description") val description: String,
    @SerializedName("bullets") val bullets: List<String>? = null
)

data class Education(
    @SerializedName("institution") val institution: String,
    @SerializedName("degree") val degree: String,
    @SerializedName("field_of_study") val fieldOfStudy: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null
)

data class Project(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String? = null
)

data class ResumeGenerationResponse(
    @SerializedName("resume_id") val resumeId: String,
    @SerializedName("status") val status: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("template") val template: String
)

