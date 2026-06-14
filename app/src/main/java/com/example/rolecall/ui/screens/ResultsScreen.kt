package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
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
        viewModelScope.launch {
            repository.saveJob(job)
        }
    }

    fun deleteJob(job: JobItem) {
        viewModelScope.launch {
            repository.deleteSavedJob(job)
        }
    }
}

@Composable
fun ResultsScreen(navController: NavController) {
    val viewModel: ResultsViewModel = hiltViewModel()
    val mockJobs = remember { MockData.getMockJobs() }
    val savedJobs by viewModel.savedJobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredJobs = remember(mockJobs, searchQuery) {
        if (searchQuery.isBlank()) mockJobs
        else mockJobs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.company.contains(searchQuery, ignoreCase = true)
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = "Your Matches",
        showSearchBar = true,
        onSearchQueryChanged = { searchQuery = it }
    ) { modifier ->
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
                        if (isJobSaved) {
                            viewModel.deleteJob(job)
                        } else {
                            viewModel.saveJob(job)
                        }
                    }
                )
            }
        }
    }
}