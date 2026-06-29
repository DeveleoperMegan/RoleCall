package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.mock.MockData
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.SecondaryText
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun saveJob(job: JobItem) {
        viewModelScope.launch { repository.saveJob(job) }
    }

    fun deleteJob(job: JobItem) {
        viewModelScope.launch { repository.deleteSavedJob(job) }
    }
}

@Composable
fun ResultsScreen(
    navController: NavController,
    matchHistoryId: Long? = null
) {
    val viewModel: ResultsViewModel = hiltViewModel()
    val savedJobs by viewModel.savedJobs.collectAsState()

    val searchQuery = navController.currentBackStackEntry
        ?.arguments
        ?.getString("query") ?: ""

    var localSearchQuery by remember { mutableStateOf(searchQuery) }

    val allJobs = remember { MockData.getMockJobs() }

    val filteredJobs = remember(allJobs, localSearchQuery) {
        if (localSearchQuery.isBlank()) allJobs
        else allJobs.filter {
            it.title.contains(localSearchQuery, ignoreCase = true) ||
                    it.company.contains(localSearchQuery, ignoreCase = true) ||
                    it.location.contains(localSearchQuery, ignoreCase = true)
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = if (matchHistoryId != null) "Past Results" else "Your Matches",
        showSearchBar = true,
        onSearchQueryChanged = { query ->
            localSearchQuery = query
        }
    ) { modifier ->
        if (filteredJobs.isEmpty()) {
            Box(
                modifier = modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No jobs match your search.",
                    color = SecondaryText
                )
            }
        } else {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredJobs) { job ->
                    val isJobSaved = savedJobs.any { it.id == job.id }
                    JobCard(
                        job = job,
                        onClick = {
                            navController.navigate("job_detail/${job.id}")
                        },
                        isSaved = isJobSaved,
                        onSaveClick = {
                            if (isJobSaved) viewModel.deleteJob(job)
                            else viewModel.saveJob(job)
                        }
                    )
                }
            }
        }
    }
}