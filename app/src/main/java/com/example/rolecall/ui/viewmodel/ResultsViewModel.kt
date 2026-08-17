package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    val savedJobs: StateFlow<List<JobItem>> = repository.getAllSavedJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var allMatchResults by mutableStateOf<List<JobItem>>(emptyList())
    var displayedJobs by mutableStateOf<List<JobItem>>(emptyList())
        private set
    var isLoadingMore by mutableStateOf(false)
        private set
    var hasMorePages by mutableStateOf(true)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    private val pageSize = 10
    private var currentPage = 0

    fun setMatchResults(jobs: List<JobItem>) {
        allMatchResults = jobs
        currentPage = 0
        displayedJobs = jobs.take(pageSize)
        hasMorePages = jobs.size > pageSize
    }

    fun loadNextPage() {
        if (isLoadingMore || !hasMorePages) return
        viewModelScope.launch {
            isLoadingMore = true
            delay(300)
            currentPage++
            val start = currentPage * pageSize
            val end = minOf(start + pageSize, allMatchResults.size)
            displayedJobs = allMatchResults.take(end)
            hasMorePages = end < allMatchResults.size
            isLoadingMore = false
        }
    }

    fun refresh() {
        if (isRefreshing) return
        viewModelScope.launch {
            isRefreshing = true
            delay(1000)  // simulate network delay; replace with real re‑fetch later
            currentPage = 0
            displayedJobs = allMatchResults.take(pageSize)
            hasMorePages = allMatchResults.size > pageSize
            isRefreshing = false
        }
    }

    fun saveJob(job: JobItem) {
        viewModelScope.launch { repository.saveJob(job) }
    }

    fun deleteJob(job: JobItem) {
        viewModelScope.launch { repository.deleteSavedJob(job) }
    }

    fun recordMatch(resumeId: Long, jobId: String, score: Double) {
        viewModelScope.launch {
            repository.recordMatch(resumeId, jobId, score)
        }
    }
}

