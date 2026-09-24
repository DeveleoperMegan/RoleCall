package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.local.dao.GeneratedResumeDao
import com.example.rolecall.data.local.entity.GeneratedResumeEntity
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.network.FastAPIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyResumesViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository,
    private val generatedResumeDao: GeneratedResumeDao
) : ViewModel() {

    var uploadedResumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var deletingId by mutableStateOf<String?>(null)
        private set
    var renamingId by mutableStateOf<String?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val generatedResumes: StateFlow<List<GeneratedResumeEntity>> =
        generatedResumeDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadUploadedResumes()
    }

    fun loadUploadedResumes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            fastAPIRepository.getResumes()
                .onSuccess { uploadedResumes = it }
                .onFailure { errorMessage = it.message ?: "Failed to load resumes" }
            isLoading = false
        }
    }

    fun deleteUploadedResume(resumeId: String) {
        viewModelScope.launch {
            deletingId = resumeId
            fastAPIRepository.deleteResume(resumeId)
                .onSuccess { uploadedResumes = uploadedResumes.filter { r -> r.id != resumeId } }
                .onFailure { errorMessage = it.message ?: "Delete failed" }
            deletingId = null
        }
    }

    fun deleteGeneratedResume(entity: GeneratedResumeEntity) {
        viewModelScope.launch {
            deletingId = entity.id
            generatedResumeDao.delete(entity)
            deletingId = null
        }
    }

    /** Rename an uploaded resume via the backend. */
    fun renameUploadedResume(resumeId: String, newName: String) {
        viewModelScope.launch {
            renamingId = resumeId
            errorMessage = null
            fastAPIRepository.renameResume(resumeId, newName)
                .onSuccess {
                    uploadedResumes = uploadedResumes.map { r ->
                        if (r.id == resumeId) r.copy(filename = newName) else r
                    }
                }
                .onFailure { errorMessage = it.message ?: "Rename failed" }
            renamingId = null
        }
    }

    /** Rename a locally generated resume (Room only — no backend call needed). */
    fun renameGeneratedResume(entity: GeneratedResumeEntity, newName: String) {
        viewModelScope.launch {
            renamingId = entity.id
            generatedResumeDao.updateFilename(entity.id, newName)
            renamingId = null
        }
    }
}

