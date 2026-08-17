package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.SecondaryText
import com.example.rolecall.ui.viewmodel.SearchResultsUiState
import com.example.rolecall.ui.viewmodel.SearchResultsViewModel

@Composable
fun SearchResultsScreen(
    navController: NavController,
    query: String
) {
    val viewModel: SearchResultsViewModel = hiltViewModel()

    LaunchedEffect(query) {
        viewModel.search(query)
    }

    RoleCallScaffold(
        navController = navController,
        title = "Search Results",
        showSearchBar = true
    ) { modifier ->
        when (val state = viewModel.uiState) {
            is SearchResultsUiState.Loading -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SearchResultsUiState.Error -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = SecondaryText)
                }
            }
            is SearchResultsUiState.Success -> {
                if (state.jobs.isEmpty()) {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No jobs found for \"$query\"", color = SecondaryText)
                    }
                } else {
                    LazyColumn(
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.jobs) { job ->
                            JobCard(
                                job = job,
                                onClick = {
                                    val jobJson = com.google.gson.Gson().toJson(job)
                                    val encoded = java.net.URLEncoder.encode(jobJson, "UTF-8")
                                    navController.navigate("job_detail/$encoded")
                                },
                                isSaved = false,
                                onSaveClick = { /* optional: save without login */ }
                            )
                        }
                    }
                }
            }
        }
    }
}