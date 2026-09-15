package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.remote.*
import com.example.rolecall.network.ResumeCreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResumeBuilderUiState {
    data object Idle : ResumeBuilderUiState()
    data object Loading : ResumeBuilderUiState()
    data class Success(val resumeId: String, val downloadUrl: String) : ResumeBuilderUiState()
    data class Error(val message: String) : ResumeBuilderUiState()
}

@HiltViewModel
class ResumeBuilderViewModel @Inject constructor(
    private val repository: ResumeCreationRepository
) : ViewModel() {

    var uiState by mutableStateOf<ResumeBuilderUiState>(ResumeBuilderUiState.Idle)
        private set

    var contact by mutableStateOf(ContactInfo("", ""))
        private set
    var summary by mutableStateOf("")
        private set
    var workExperience by mutableStateOf(listOf<WorkExperience>())
        private set
    var education by mutableStateOf(listOf<Education>())
        private set
    var skills by mutableStateOf(listOf<String>())
        private set
    var projects by mutableStateOf(listOf<Project>())
        private set
    var certifications by mutableStateOf(listOf<String>())
        private set
    var template by mutableStateOf("classic")
        private set

    fun updateContact(new: ContactInfo) { contact = new }
    fun updateSummary(new: String) { summary = new }
    fun addWorkExperience(item: WorkExperience) { workExperience = workExperience + item }
    fun removeWorkExperience(index: Int) { workExperience = workExperience.filterIndexed { i, _ -> i != index } }
    fun addEducation(item: Education) { education = education + item }
    fun removeEducation(index: Int) { education = education.filterIndexed { i, _ -> i != index } }
    fun updateSkills(new: List<String>) { skills = new }
    fun addProject(item: Project) { projects = projects + item }
    fun removeProject(index: Int) { projects = projects.filterIndexed { i, _ -> i != index } }
    fun updateCertifications(new: List<String>) { certifications = new }
    fun selectTemplate(t: String) { template = t }

    /**
     * Prefill the resume builder from the user's profile.
     *
     * @param overwrite when false (default), only blank fields are filled.
     *                  when true, everything is overwritten (used by the manual button).
     */
    fun prefillFromProfile(
        fullName: String,
        email: String,
        title: String?,
        overwrite: Boolean = false
    ) {
        val newName  = if (overwrite || contact.name.isBlank())  fullName else contact.name
        val newEmail = if (overwrite || contact.email.isBlank()) email    else contact.email

        contact = contact.copy(name = newName, email = newEmail)

        if ((overwrite || summary.isBlank()) && !title.isNullOrBlank()) {
            summary = "$title with a proven track record of delivering results."
        }
    }
    fun loadTemplates(onLoaded: (List<String>) -> Unit) {
        viewModelScope.launch {
            repository.getTemplates()
                .onSuccess { onLoaded(it) }
                .onFailure { onLoaded(listOf("classic")) } // safe fallback
        }
    }
    fun generateResume() {
        //Validate required fields before calling the API
        if (contact.name.isBlank()) {
            uiState = ResumeBuilderUiState.Error("Please enter your full name.")
            return
        }
        if (contact.email.isBlank() || !contact.email.contains("@")) {
            uiState = ResumeBuilderUiState.Error("Please enter a valid email address.")
            return
        }
        if (workExperience.isEmpty()) {
            uiState = ResumeBuilderUiState.Error("Please add at least one work experience.")
            return
        }
        if (education.isEmpty()) {
            uiState = ResumeBuilderUiState.Error("Please add at least one education entry.")
            return
        }

        viewModelScope.launch {
            uiState = ResumeBuilderUiState.Loading
            val request = ResumeGenerationRequest(
                contact = contact,
                summary = summary.ifBlank { null },
                workExperience = workExperience,
                education = education,
                skills = skills,
                projects = projects.ifEmpty { null },
                certifications = certifications.ifEmpty { null },
                template = template
            )
            repository.generateResume(request)
                .onSuccess { response ->
                    uiState = ResumeBuilderUiState.Success(response.resumeId, response.downloadUrl)
                }
                .onFailure { error ->
                    uiState = ResumeBuilderUiState.Error(error.message ?: "Generation failed")
                }
        }
    }
}