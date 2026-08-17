package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.network.FastAPIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
                .onFailure { /* optionally show error */ }
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
}
