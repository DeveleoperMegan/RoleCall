package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    var selectedStatus by mutableStateOf("applied")
        private set

    val jobs: StateFlow<List<JobItem>> = repository.getJobsByStatus(selectedStatus)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectStatus(status: String) {
        selectedStatus = status
    }

    fun updateStatus(jobId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateJobStatus(jobId, newStatus)
        }
    }
}

