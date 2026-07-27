package com.example.rolecall.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.rolecall.ui.screens.SearchJsonResponse
import com.example.rolecall.ui.screens.MatchJsonItem

@HiltViewModel
class ResumeListViewModel @Inject constructor(
    val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    var resumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var deletingId by mutableStateOf<String?>(null)
        private set
    var renamingId by mutableStateOf<String?>(null)
        private set
    var renameError by mutableStateOf<String?>(null)
        private set

    init {
        loadResumes()
    }

    fun loadResumes() {
        viewModelScope.launch {
            isLoading = true
            val list = withContext(Dispatchers.IO) {
                fastAPIRepository.getResumes()
            }
            resumes = list ?: emptyList()
            isLoading = false
        }
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch {
            deletingId = resumeId
            val success = withContext(Dispatchers.IO) {
                fastAPIRepository.deleteResume(resumeId)
            }
            if (success) {
                resumes = resumes.filter { it.id != resumeId }
            }
            deletingId = null
        }
    }

    fun renameResume(resumeId: String, newName: String) {
        viewModelScope.launch {
            renamingId = resumeId
            renameError = null
            val success = withContext(Dispatchers.IO) {
                fastAPIRepository.renameResume(resumeId, newName)
            }
            if (success) {
                // Update local list with new filename
                resumes = resumes.map {
                    if (it.id == resumeId) it.copy(filename = newName) else it
                }
            } else {
                renameError = "Could not rename file"
            }
            renamingId = null
        }
    }

    fun searchResume(resumeId: String, navController: NavController) {
        // Reuse the same logic as UploadScreen – just navigate to results
        viewModelScope.launch {
            val searchJson = withContext(Dispatchers.IO) {
                fastAPIRepository.searchJobs(resumeId)
            }
            if (searchJson != null) {
                // Parse and pass results – we'll reuse MatchResultsHolder from UploadScreen
                val gson = com.google.gson.Gson()
                val searchResponse = try {
                    gson.fromJson(searchJson, SearchJsonResponse::class.java)
                } catch (e: Exception) { null }

                val jobs = searchResponse?.matches?.map { match ->
                    com.example.rolecall.data.model.JobItem(
                        id = match.id,
                        title = match.title,
                        company = match.companyName ?: "Unknown",
                        location = "",
                        description = match.description,
                        matchScore = (match.similarity * 100).toFloat(),
                        maxSalary = match.maxSalary,
                        minSalary = match.minSalary,
                        postDate = match.postDate,
                        postUrl = match.postUrl
                    )
                } ?: emptyList()

                MatchResultsHolder.setResults(jobs)
                navController.navigate(com.example.rolecall.navigation.Routes.RESULTS) {
                    popUpTo(com.example.rolecall.navigation.Routes.UPLOAD) { inclusive = false }
                }
            }
        }
    }
}

@Composable
fun ResumeListScreen(navController: NavController) {
    val viewModel: ResumeListViewModel = hiltViewModel()
    val context = LocalContext.current

    RoleCallScaffold(
        navController = navController,
        title = "My Resumes",
        showSearchBar = false
    ) { modifier ->
        if (viewModel.isLoading) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viewModel.resumes.isEmpty()) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No resumes uploaded yet.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.resumes) { resume ->
                    val isDeleting = viewModel.deletingId == resume.id
                    val isRenaming = viewModel.renamingId == resume.id
                    var showRenameDialog by remember { mutableStateOf(false) }

                    ResumeCard(
                        resume = resume,
                        onSearch = { viewModel.searchResume(resume.id, navController) },
                        onDelete = { viewModel.deleteResume(resume.id) },
                        onPreview = {
                            // Fetch detail to get file URL, then open it
                            viewModel.viewModelScope.launch {
                                val detail = withContext(Dispatchers.IO) {
                                    viewModel.fastAPIRepository.getResumeDetail(resume.id)
                                }
                                detail?.let {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.fileUrl))
                                    context.startActivity(intent)
                                }
                            }
                        },
                        onRenameRequest = { showRenameDialog = true },
                        isDeleting = isDeleting
                    )

                    if (showRenameDialog) {
                        var newName by remember { mutableStateOf(resume.filename) }
                        AlertDialog(
                            onDismissRequest = { showRenameDialog = false },
                            title = { Text("Rename Resume") },
                            text = {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    singleLine = true,
                                    label = { Text("File name") }
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.renameResume(resume.id, newName)
                                        showRenameDialog = false
                                    },
                                    enabled = newName.isNotBlank() && !isRenaming
                                ) {
                                    Text("Rename")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRenameDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeCard(
    resume: ResumeItem,
    onSearch: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit,
    onRenameRequest: () -> Unit,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearch() },   // Whole card triggers search
        colors = CardDefaults.cardColors(containerColor = FoundationSurface),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SecondaryText),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile picture",
                    tint = PrimaryText,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File name and date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resume.filename,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryText
                )
                Text(
                    text = "Uploaded: ${resume.createdAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            // Action buttons: Preview, Rename, Delete
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onPreview) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Preview file",
                        tint = UiInteractive
                    )
                }
                IconButton(onClick = onRenameRequest) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Rename file",
                        tint = UiInteractive
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete resume",
                        tint = AccentAlert
                    )
                }
            }
        }
    }
}