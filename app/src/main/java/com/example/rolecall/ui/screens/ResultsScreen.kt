package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.SecondaryText
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

@Composable
fun ResultsScreen(
    navController: NavController,
    matchHistoryId: Long? = null
) {
    val viewModel: ResultsViewModel = hiltViewModel()

    val savedJobs by viewModel.savedJobs.collectAsState()
    val displayedJobs = viewModel.displayedJobs
    val isLoadingMore = viewModel.isLoadingMore
    val hasMorePages = viewModel.hasMorePages

    val listState = rememberLazyListState()

    val searchQuery = navController.currentBackStackEntry
        ?.arguments
        ?.getString("query") ?: ""

    var localSearchQuery by remember { mutableStateOf(searchQuery) }

    // Collect results from the singleton holder
    val matchResults by MatchResultsHolder.results.collectAsState()

    LaunchedEffect(matchResults) {
        if (matchResults.isNotEmpty()) {
            viewModel.setMatchResults(matchResults)
        }
    }

    val filteredJobs = remember(displayedJobs, localSearchQuery) {
        if (localSearchQuery.isBlank()) displayedJobs
        else displayedJobs.filter {
            it.title.contains(localSearchQuery, ignoreCase = true) ||
                    it.company.contains(localSearchQuery, ignoreCase = true) ||
                    it.location.contains(localSearchQuery, ignoreCase = true)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasMorePages && !isLoadingMore) {
            viewModel.loadNextPage()
        }
    }

    LaunchedEffect(displayedJobs) {
        if (displayedJobs.isNotEmpty()) {
            displayedJobs.take(3).forEach { job ->
                viewModel.recordMatch(
                    resumeId = 0L,
                    jobId = job.id,
                    score = (job.matchScore / 100.0)
                )
            }
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = if (matchHistoryId != null) "Past Results" else "Your Matches",
        showSearchBar = true,
        onSearchQueryChanged = { query -> localSearchQuery = query }
    ) { modifier ->
        if (filteredJobs.isEmpty() && !isLoadingMore) {
            Box(
                modifier = modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No jobs match your search.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredJobs) { job ->
                    val isJobSaved = savedJobs.any { it.id == job.id }
                    JobCard(
                        job = job,
                        onClick = {
                            val jobJson = Gson().toJson(job)
                            val encodedJson = URLEncoder.encode(jobJson, StandardCharsets.UTF_8.toString())
                            navController.navigate("job_detail/$encodedJson")
                        },
                        isSaved = isJobSaved,
                        onSaveClick = {
                            if (isJobSaved) viewModel.deleteJob(job)
                            else viewModel.saveJob(job)
                        }
                    )
                }

                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (!hasMorePages && filteredJobs.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "All results loaded",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }
        }
    }
}