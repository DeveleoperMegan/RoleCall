package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.remote.*
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.screens.MatchResultsHolder
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ResumeListViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    var resumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var deletingId by mutableStateOf<String?>(null)
        private set
    var renamingId by mutableStateOf<String?>(null)
        private set
    var renameError by mutableStateOf<String?>(null)
        private set

    private val gson = Gson()

    init {
        loadResumes()
    }

    fun loadResumes() {
        viewModelScope.launch {
            isLoading = true
            fastAPIRepository.getResumes()
                .onSuccess { resumes = it }
                .onFailure { resumes = emptyList() }
            isLoading = false
        }
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            deletingId = resumeId
            fastAPIRepository.deleteResume(resumeId)
                .onSuccess { resumes = resumes.filter { r -> r.id != resumeId } }
                .onFailure { /* show error */ }
            deletingId = null
        }
    }

    fun renameResume(resumeId: String, newName: String) {
        viewModelScope.launch {
            renamingId = resumeId
            renameError = null
            fastAPIRepository.renameResume(resumeId, newName)
                .onSuccess {
                    resumes = resumes.map { r ->
                        if (r.id == resumeId) r.copy(filename = newName) else r
                    }
                }
                .onFailure { e -> renameError = e.message ?: "Rename failed" }
            renamingId = null
        }
    }

    fun searchResume(resumeId: String, navController: NavController) {
        viewModelScope.launch {
            fastAPIRepository.searchJobs(resumeId)
                .onSuccess { json ->
                    val searchResponse = try {
                        gson.fromJson(json, SearchJsonResponse::class.java)
                    } catch (e: Exception) { null }
                    val jobs = searchResponse?.matches?.map { match ->
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
                            postUrl = match.postUrl
                        )
                    } ?: emptyList()
                    MatchResultsHolder.setResults(jobs)
                    navController.navigate(Routes.RESULTS) {
                        popUpTo(Routes.UPLOAD) { inclusive = false }
                    }
                }
        }
    }

    suspend fun getPreviewUrl(resumeId: String): String? {
        var fileUrl: String? = null
        withContext(Dispatchers.IO) {
            fastAPIRepository.getResumeDetail(resumeId)
                .onSuccess { detail -> fileUrl = detail.fileUrl }
        }
        return fileUrl
    }
}
