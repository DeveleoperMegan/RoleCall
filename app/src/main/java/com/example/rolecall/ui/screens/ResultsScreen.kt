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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.SecondaryText
import com.example.rolecall.ui.viewmodel.ResultsViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import androidx.compose.foundation.layout.fillMaxSize

@OptIn(ExperimentalMaterial3Api::class)
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
    val isRefreshing = viewModel.isRefreshing

    val listState = rememberLazyListState()

    val searchQuery = navController.currentBackStackEntry
        ?.arguments
        ?.getString("query") ?: ""

    var localSearchQuery by remember { mutableStateOf(searchQuery) }

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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = modifier
        ) {
            if (filteredJobs.isEmpty() && !isLoadingMore) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No jobs match your search.", color = SecondaryText)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredJobs) { job ->
                        val isJobSaved = savedJobs.any { it.id == job.id }
                        JobCard(
                            job = job,
                            onClick = {
                                val jobJson = Gson().toJson(job)
                                val encodedJson = URLEncoder.encode(jobJson, "UTF-8")
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
}