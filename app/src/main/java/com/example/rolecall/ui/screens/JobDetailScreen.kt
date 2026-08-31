package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.ui.components.MatchBadge
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val repository: JobRepository
) : ViewModel() {

    val savedJobs: StateFlow<List<JobItem>> = repository.getAllSavedJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveJob(job: JobItem) {
        viewModelScope.launch { repository.saveJob(job) }
    }

    fun deleteJob(job: JobItem) {
        viewModelScope.launch { repository.deleteSavedJob(job) }
    }

    fun applyToJob(job: JobItem) {
        viewModelScope.launch { repository.applyToJob(job) }
    }

    fun updateJobStatus(jobId: String, newStatus: String) {
        viewModelScope.launch { repository.updateJobStatus(jobId, newStatus) }
    }
}

@Composable
fun JobDetailScreen(navController: NavController, job: JobItem?) {
    val viewModel: JobDetailViewModel = hiltViewModel()
    val savedJobs by viewModel.savedJobs.collectAsState()

    if (job == null) {
        RoleCallScaffold(navController = navController) { modifier ->
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text("Job not found.", color = PrimaryText)
            }
        }
        return
    }

    val isJobSaved = savedJobs.any { it.id == job.id }
    var applied by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(job.status) }

    RoleCallScaffold(
        navController = navController,
        title = "Job Details",
        showSearchBar = false
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(job.company, style = MaterialTheme.typography.titleMedium, color = SecondaryText)
            if (job.location.isNotBlank()) {
                Text(job.location, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                MatchBadge(score = job.matchScore)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        job.matchScore >= 90f -> "Excellent Match"
                        job.matchScore >= 70f -> "Strong Match"
                        job.matchScore >= 50f -> "Good Match"
                        else -> "Average Match"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText
                )
            }

            if (job.minSalary != null || job.maxSalary != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val salaryText = buildString {
                    if (job.minSalary != null && job.maxSalary != null) {
                        append("$${job.minSalary.toLong()} - $${job.maxSalary.toLong()}")
                    } else if (job.minSalary != null) {
                        append("From $${job.minSalary.toLong()}")
                    } else if (job.maxSalary != null) {
                        append("Up to $${job.maxSalary.toLong()}")
                    }
                }
                Text("Salary: $salaryText", style = MaterialTheme.typography.titleMedium, color = AccentSuccess)
            }

            if (job.postDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Posted: ${job.postDate}", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Key Matching Skills
            if (job.keySkills.isNotEmpty()) {
                Text(
                    "Key Matching Skills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    job.keySkills.forEach { skill ->
                        Text("• $skill", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Matching Phrases
            if (job.matchingPhrases.isNotEmpty()) {
                Text(
                    "Matching Phrases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    job.matchingPhrases.forEach { phrase ->
                        Text("→ $phrase", style = MaterialTheme.typography.bodyMedium, color = AccentSuccess)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Full Description
            Text(
                "Full Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                job.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )

            if (job.postUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { /* open browser */ }) {
                    Text("View Original Posting", color = UiInteractive)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status chips
            Text(
                "Application Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("saved", "applied", "interviewing", "offer").forEach { status ->
                    FilterChip(
                        selected = currentStatus == status,
                        onClick = {
                            currentStatus = status
                            viewModel.updateJobStatus(job.id, status)
                        },
                        label = { Text(status.replaceFirstChar { it.uppercase() }, color = PrimaryText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentSuccess,
                            selectedLabelColor = PrimaryText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save and Apply buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isJobSaved) viewModel.deleteJob(job) else viewModel.saveJob(job)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJobSaved) AccentSuccess else UiInteractive
                    )
                ) {
                    Text(if (isJobSaved) "Saved" else "Save Job")
                }

                Button(
                    onClick = {
                        if (!applied) {
                            if (!isJobSaved) viewModel.saveJob(job)
                            viewModel.applyToJob(job)
                            applied = true
                            currentStatus = "applied"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (applied) AccentSuccess else AccentAlert
                    )
                ) {
                    Text(if (applied) "Applied" else "Apply Now")
                }
            }
        }
    }
}