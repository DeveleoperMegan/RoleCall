package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rolecall.data.mock.MockData
import com.example.rolecall.ui.components.JobCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavController) {
    val mockJobs = remember { MockData.getMockJobs() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Matches") },
                actions = {
                    TextButton(onClick = {
                        navController.navigate("upload") {
                            popUpTo("upload") { inclusive = true }
                        }
                    }) {
                        Text("New Résumé")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockJobs) { job ->
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