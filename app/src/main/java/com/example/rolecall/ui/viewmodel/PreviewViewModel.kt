package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.network.FastAPIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class PreviewUiState {
    data object Loading : PreviewUiState()
    data class Success(val filePath: String, val fileType: String) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
}

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repository: FastAPIRepository
) : ViewModel() {

    var uiState by mutableStateOf<PreviewUiState>(PreviewUiState.Loading)
        private set

    fun loadPreview(resumeId: String, cacheDir: File) {
        viewModelScope.launch {
            uiState = PreviewUiState.Loading
            // First get the resume detail to have the file URL and type
            repository.getResumeDetail(resumeId)
                .onSuccess { detail ->
                    // Download the file using the authenticated client
                    repository.downloadFile(detail.fileUrl, detail.filename, cacheDir)
                        .onSuccess { filePath ->
                            uiState = PreviewUiState.Success(filePath, detail.fileType)
                        }
                        .onFailure { error ->
                            uiState = PreviewUiState.Error(error.message ?: "Download failed")
                        }
                }
                .onFailure { error ->
                    uiState = PreviewUiState.Error(error.message ?: "Failed to load file")
                }
        }
    }
}