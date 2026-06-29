package com.example.rolecall.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.local.entity.MatchHistoryEntity
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    val savedJobs: StateFlow<List<JobItem>> = repository.getAllSavedJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.getAllMatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteJob(job: JobItem) {
        viewModelScope.launch {
            repository.deleteSavedJob(job)
        }
    }
}

@Composable
fun HistoryScreen(navController: NavController) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val savedJobs by viewModel.savedJobs.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("History", "Saved")

    RoleCallScaffold(
        navController = navController,
        title = "My Jobs",
        showSearchBar = true
    ) { modifier ->
        Column(modifier = modifier) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = FoundationDark,
                contentColor = PrimaryText
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) PrimaryText else SecondaryText
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> HistoryTab(matchHistory, navController)
                1 -> SavedTab(savedJobs, navController, viewModel::deleteJob)
            }
        }
    }
}

@Composable
private fun HistoryTab(history: List<MatchHistoryEntity>, navController: NavController) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No past matches yet.", color = SecondaryText)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Navigate to Results screen with this match history ID
                            navController.navigate("results/${item.id}")
                        },
                    colors = CardDefaults.cardColors(containerColor = PrimaryText),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatDate(item.date),
                                style = MaterialTheme.typography.titleMedium,
                                color = Border
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Job ID: ${item.jobId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Border.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Match Score: ${(item.score * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Border.copy(alpha = 0.5f)
                            )
                        }

                        com.example.rolecall.ui.components.MatchBadge(
                            score = (item.score * 100).toFloat()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedTab(
    savedJobs: List<JobItem>,
    navController: NavController,
    onDeleteJob: (JobItem) -> Unit
) {
    if (savedJobs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("You haven't saved any jobs yet.", color = SecondaryText)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(savedJobs) { job ->
                JobCard(
                    job = job,
                    onClick = {
                        navController.navigate("job_detail/${job.id}")
                    },
                    isSaved = true,
                    onSaveClick = {
                        onDeleteJob(job)
                    }
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}