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
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.ApplicationsViewModel
import com.google.gson.Gson
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(navController: NavController) {
    val viewModel: ApplicationsViewModel = hiltViewModel()
    val jobs by viewModel.jobs.collectAsState()

    val statuses = listOf("saved", "applied", "interviewing", "offer")

    RoleCallScaffold(
        navController = navController,
        title = "My Applications",
        showSearchBar = false
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize()) {
            // Status tabs
            ScrollableTabRow(
                selectedTabIndex = statuses.indexOf(viewModel.selectedStatus).coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                statuses.forEach { status ->
                    Tab(
                        selected = viewModel.selectedStatus == status,
                        onClick = { viewModel.selectStatus(status) },
                        text = { Text(status.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Job list
            if (jobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No ${viewModel.selectedStatus} jobs yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jobs) { job ->
                        JobCard(
                            job = job,
                            onClick = {
                                val json = Gson().toJson(job)
                                val encoded = URLEncoder.encode(json, "UTF-8")
                                navController.navigate("job_detail/$encoded")
                            },
                            isSaved = true,
                            onSaveClick = {
                                // Maybe offer remove from list? keep simple for now
                            }
                        )
                    }
                }
            }
        }
    }
}

