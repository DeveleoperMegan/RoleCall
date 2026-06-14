package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rolecall.data.mock.MockData
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.MatchBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    fun saveJob(job: com.example.rolecall.data.model.JobItem) {
        viewModelScope.launch {
            repository.saveJob(job)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(navController: NavController, jobId: String) {
    val viewModel: JobDetailViewModel = hiltViewModel()

    val job = remember(jobId) {
        MockData.getMockJobs().find { it.id == jobId }
    }

    if (job == null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Job Details") }) }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Job not found.")
            }
        }
        return
    }

    val mockPhrases = remember {
        listOf(
            "3 years of Kotlin development",
            "Experience with Jetpack Compose",
            "Team leadership background"
        )
    }

    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveJob(job)
                    saved = true
                }
            ) {
                Text(if (saved) "Saved!" else "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = job.company,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = job.location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                MatchBadge(score = job.matchScore)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        job.matchScore >= 80f -> "Strong Match"
                        job.matchScore >= 60f -> "Good Match"
                        else -> "Average Match"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Why this match?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            mockPhrases.forEach { phrase ->
                Text(
                    text = "• $phrase",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Full Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We are seeking a skilled ${job.title} to join our growing team. " +
                        "The ideal candidate has a strong background in mobile development " +
                        "and a passion for building intuitive user experiences. " +
                        "You will work closely with product managers and designers to " +
                        "deliver high‑quality applications that scale.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}