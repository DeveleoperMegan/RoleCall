package com.example.rolecall.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Simple model for a past match session – you can later move this to data/model/
data class MatchHistoryItem(
    val date: String,
    val topMatchTitle: String,
    val topMatchScore: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("History", "Saved")

    // Mock history – in the future this will come from Room
    val mockHistory = remember {
        listOf(
            MatchHistoryItem("2025-05-20", "Android Developer", 92f),
            MatchHistoryItem("2025-05-18", "Backend Engineer", 85f),
            MatchHistoryItem("2025-05-15", "Data Scientist", 78f)
        )
    }

    // Mock saved jobs – will be populated from Room later
    val mockSavedJobs = remember { mutableStateListOf<com.example.rolecall.data.model.JobItem>() }

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
                1 -> SavedTab(mockSavedJobs, navController)
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
            .clickable {
                // Optionally navigate to results screen with that session – not implemented yet
            }
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
        com.example.rolecall.ui.components.MatchBadge(score = item.topMatchScore)
    }
}

@Composable
private fun SavedTab(savedJobs: List<com.example.rolecall.data.model.JobItem>, navController: NavController) {
    if (savedJobs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("You haven't saved any jobs yet.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(savedJobs.size) { index ->
                com.example.rolecall.ui.components.JobCard(
                    job = savedJobs[index],
                    onClick = {
                        navController.navigate("job_detail/${savedJobs[index].id}")
                    }
                )
            }
        }
    }
}