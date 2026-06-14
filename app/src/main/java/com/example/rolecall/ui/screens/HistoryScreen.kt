package com.example.rolecall.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.MatchBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchHistoryItem(
    val date: String,
    val topMatchTitle: String,
    val topMatchScore: Float
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    val savedJobs: StateFlow<List<JobItem>> = repository.getAllSavedJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val savedJobs by viewModel.savedJobs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("History", "Saved")

    val mockHistory = remember {
        listOf(
            MatchHistoryItem("2025-05-20", "Android Developer", 92f),
            MatchHistoryItem("2025-05-18", "Backend Engineer", 85f),
            MatchHistoryItem("2025-05-15", "Data Scientist", 78f)
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Jobs") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> HistoryTab(mockHistory)
                1 -> SavedTab(savedJobs, navController)
            }
        }
    }
}

@Composable
private fun HistoryTab(history: List<MatchHistoryItem>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No past matches yet.")
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            history.forEach { item ->
                HistoryCard(item)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun HistoryCard(item: MatchHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.date, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Top match: ${item.topMatchTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MatchBadge(score = item.topMatchScore)
    }
}

@Composable
private fun SavedTab(savedJobs: List<JobItem>, navController: NavController) {
    if (savedJobs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("You haven't saved any jobs yet.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(savedJobs) { job ->
                JobCard(
                    job = job,
                    onClick = {
                        navController.navigate("job_detail/${job.id}")
                    }
                )
            }
        }
    }
}