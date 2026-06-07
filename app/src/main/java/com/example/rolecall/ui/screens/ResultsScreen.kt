package com.example.rolecall.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
                    //Placeholder for future "New Résumé" action
                    TextButton(onClick = {
                        // Navigate back to upload screen
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
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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