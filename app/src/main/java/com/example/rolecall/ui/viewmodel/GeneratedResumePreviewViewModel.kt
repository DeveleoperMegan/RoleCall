package com.example.rolecall.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.local.dao.GeneratedResumeDao
import com.example.rolecall.data.local.entity.GeneratedResumeEntity
import com.example.rolecall.network.ResumeCreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class GeneratedPreviewState {
    data object Loading : GeneratedPreviewState()
    data class Success(val filePath: String) : GeneratedPreviewState()
    data class Error(val message: String) : GeneratedPreviewState()
}

@HiltViewModel
class GeneratedResumePreviewViewModel @Inject constructor(
    private val repository: ResumeCreationRepository,
    private val generatedResumeDao: GeneratedResumeDao,
    application: Application
) : AndroidViewModel(application) {

    var state by mutableStateOf<GeneratedPreviewState>(GeneratedPreviewState.Loading)
        private set

    private val generatedDir: File = File(application.filesDir, "generated_resumes").apply { mkdirs() }

    fun load(resumeId: String, template: String = "classic") {
        viewModelScope.launch {
            state = GeneratedPreviewState.Loading

            // Check Room first — if we already saved it, just show it
            val existing = withContext(Dispatchers.IO) {
                generatedResumeDao.getById(resumeId)
            }
            if (existing != null && File(existing.localPath).exists()) {
                state = GeneratedPreviewState.Success(existing.localPath)
                return@launch
            }

            // Download from backend into the persistent directory
            repository.downloadResume(resumeId, generatedDir)
                .onSuccess { path ->
                    val target = File(generatedDir, "resume_$resumeId.pdf")
                    File(path).renameTo(target)

                    withContext(Dispatchers.IO) {
                        generatedResumeDao.insert(
                            GeneratedResumeEntity(
                                id = resumeId,
                                filename = "resume_$resumeId.pdf",
                                template = template,
                                localPath = target.absolutePath
                            )
                        )
                    }
                    state = GeneratedPreviewState.Success(target.absolutePath)
                }
                .onFailure { error ->
                    state = GeneratedPreviewState.Error(error.message ?: "Download failed")
                }
        }
    }

    fun delete(resumeId: String) {
        viewModelScope.launch {
            val entity = withContext(Dispatchers.IO) { generatedResumeDao.getById(resumeId) }
            entity?.let {
                File(it.localPath).delete()
                withContext(Dispatchers.IO) { generatedResumeDao.delete(it) }
            }
        }
    }
}

