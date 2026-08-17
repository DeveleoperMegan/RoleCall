package com.example.rolecall.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.data.remote.UploadJsonResponse
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val filename: String, val textLength: Int) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    var uploadState by mutableStateOf<UploadUiState>(UploadUiState.Idle)
        private set

    var savedResumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoadingResumes by mutableStateOf(false)
        private set

    fun loadSavedResumes() {
        viewModelScope.launch {
            isLoadingResumes = true
            fastAPIRepository.getResumes()
                .onSuccess { resumes -> savedResumes = resumes }
                .onFailure { savedResumes = emptyList() }
            isLoadingResumes = false
        }
    }

    fun uploadAndNavigateToAnimation(file: File, mimeType: String, navController: NavController) {
        viewModelScope.launch {
            uploadState = UploadUiState.Loading
            fastAPIRepository.uploadResume(file, mimeType)
                .onSuccess { uploadResponse ->
                    uploadState = UploadUiState.Success(uploadResponse.filename, uploadResponse.textLength)
                    navController.navigate("matching_animation/${uploadResponse.resumeId}") {
                        popUpTo(Routes.UPLOAD) { inclusive = false }
                    }
                }
                .onFailure { error ->
                    uploadState = UploadUiState.Error(error.message ?: "Upload failed")
                }
        }
    }
}