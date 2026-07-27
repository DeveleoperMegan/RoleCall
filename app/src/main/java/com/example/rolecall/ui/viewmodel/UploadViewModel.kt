package com.example.rolecall.ui.viewmodel

import android.util.Log
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
import java.io.File
import javax.inject.Inject

// ── UI state classes (stay with the ViewModel) ───────────────────────────────
sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val filename: String, val textLength: Int) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

sealed class MatchUiState {
    data object Idle : MatchUiState()
    data object Loading : MatchUiState()
    data class Success(val matchCount: Int) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    var uploadState by mutableStateOf<UploadUiState>(UploadUiState.Idle)
        private set
    var matchState by mutableStateOf<MatchUiState>(MatchUiState.Idle)
        private set

    var savedResumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoadingResumes by mutableStateOf(false)
        private set

    private val gson = Gson()

    fun loadSavedResumes() {
        viewModelScope.launch {
            isLoadingResumes = true
            fastAPIRepository.getResumes()
                .onSuccess { resumes -> savedResumes = resumes }
                .onFailure { savedResumes = emptyList() }
            isLoadingResumes = false
        }
    }

    fun searchExistingResume(resumeId: String, navController: NavController) {
        navController.navigate("matching_animation/$resumeId") {
            popUpTo(Routes.UPLOAD) { inclusive = false }
        }
    }

    fun uploadAndSearch(file: File, mimeType: String, navController: NavController) {
        viewModelScope.launch {
            uploadState = UploadUiState.Loading
            fastAPIRepository.uploadResume(file, mimeType)
                .onSuccess { uploadResponse ->
                    uploadState = UploadUiState.Success(uploadResponse.filename, uploadResponse.textLength)
                    // Navigate to full‑screen animation
                    navController.navigate("matching_animation/${uploadResponse.resumeId}") {
                        popUpTo(Routes.UPLOAD) { inclusive = false }
                    }
                }
                .onFailure { error ->
                    uploadState = UploadUiState.Error(error.message ?: "Upload failed")
                }
        }
    }

    private fun processSearchJson(json: String) {
        val searchResponse = try {
            gson.fromJson(json, SearchJsonResponse::class.java)
        } catch (e: Exception) {
            Log.e("UPLOAD_DEBUG", "Parse error: ${e.message}")
            matchState = MatchUiState.Error("Failed to parse results")
            return
        }
        val matchList = searchResponse.matches ?: emptyList()
        Log.i("UPLOAD_DEBUG", "Final match list size: ${matchList.size}")
        val jobs = matchList.map { match ->
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
        }
        matchState = MatchUiState.Success(jobs.size)
        MatchResultsHolder.setResults(jobs)
    }
}