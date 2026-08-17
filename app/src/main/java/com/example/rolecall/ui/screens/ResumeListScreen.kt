package com.example.rolecall.ui.screens

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
import androidx.navigation.NavController
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.ResumeListViewModel

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
                items(viewModel.resumes) { resume: ResumeItem ->
                    val isDeleting = viewModel.deletingId == resume.id
                    var showRenameDialog by remember { mutableStateOf(false) }

                    ResumeCard(
                        resume = resume,
                        onSearch = { navController.navigate("matching_animation/${resume.id}") },
                        onDelete = { viewModel.deleteResume(resume.id) },
                        onPreview = {
                            navController.navigate("preview/${resume.id}")
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
                                    enabled = newName.isNotBlank() && viewModel.renamingId != resume.id
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
            .clickable { onSearch() },
        colors = CardDefaults.cardColors(containerColor = FoundationSurface),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onPreview) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview file", tint = UiInteractive)
                }
                IconButton(onClick = onRenameRequest) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename file", tint = UiInteractive)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete resume", tint = AccentAlert)
                }
            }
        }
    }
}