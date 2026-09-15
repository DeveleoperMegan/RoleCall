package com.example.rolecall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.data.local.entity.GeneratedResumeEntity
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.MyResumesViewModel

@Composable
fun MyResumesScreen(navController: NavController) {
    val viewModel: MyResumesViewModel = hiltViewModel()
    val generatedResumes by viewModel.generatedResumes.collectAsState()

    val allEmpty = viewModel.uploadedResumes.isEmpty() && generatedResumes.isEmpty()

    // Dialog state
    var renameTarget by remember { mutableStateOf<RenameTarget?>(null) }

    RoleCallScaffold(
        navController = navController,
        title = "My Resumes",
        showSearchBar = false
    ) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

            // ── Generated resumes ──────────────────────────────────────────
            if (generatedResumes.isNotEmpty()) {
                Text(
                    "Generated Resumes",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(generatedResumes) { gen ->
                        ResumeManageCard(
                            filename = gen.filename,
                            subtitle = "Template: ${gen.template}",
                            isGenerated = true,
                            isDeleting = viewModel.deletingId == gen.id,
                            onSearch = { navController.navigate("matching_animation/${gen.id}") },
                            onPreview = { navController.navigate("preview_local/${gen.id}") },
                            onRename = {
                                renameTarget = RenameTarget.Generated(gen, gen.filename)
                            },
                            onDelete = { viewModel.deleteGeneratedResume(gen) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Uploaded resumes ───────────────────────────────────────────
            if (viewModel.uploadedResumes.isNotEmpty()) {
                Text(
                    "Uploaded Resumes",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.uploadedResumes) { resume ->
                        ResumeManageCard(
                            filename = resume.filename,
                            subtitle = "Uploaded: ${resume.createdAt.take(10)}",
                            isGenerated = false,
                            isDeleting = viewModel.deletingId == resume.id,
                            onSearch = { navController.navigate("matching_animation/${resume.id}") },
                            onPreview = { navController.navigate("preview/${resume.id}") },
                            onRename = {
                                renameTarget = RenameTarget.Uploaded(resume.id, resume.filename)
                            },
                            onDelete = { viewModel.deleteUploadedResume(resume.id) }
                        )
                    }
                }
            }

            if (allEmpty && !viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No resumes yet.", color = SecondaryText)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navController.navigate(Routes.UPLOAD) }) {
                            Text("Upload Resume")
                        }
                        OutlinedButton(
                            onClick = { navController.navigate(Routes.RESUME_BUILDER) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Build a Resume")
                        }
                    }
                }
            }

            viewModel.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = AccentAlert, style = MaterialTheme.typography.bodySmall)
            }
        }

        // ── Rename dialog ──────────────────────────────────────────────────
        renameTarget?.let { target ->
            RenameDialog(
                initialName = target.currentName,
                onDismiss = { renameTarget = null },
                onConfirm = { newName ->
                    when (target) {
                        is RenameTarget.Generated -> viewModel.renameGeneratedResume(target.entity, newName)
                        is RenameTarget.Uploaded  -> viewModel.renameUploadedResume(target.id, newName)
                    }
                    renameTarget = null
                }
            )
        }
    }
}

// ── Supporting types ─────────────────────────────────────────────────────────

private sealed interface RenameTarget {
    val currentName: String

    data class Generated(val entity: GeneratedResumeEntity, override val currentName: String) : RenameTarget
    data class Uploaded(val id: String, override val currentName: String) : RenameTarget
}

@Composable
private fun RenameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Resume") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("File name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text("Rename", color = UiInteractive)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SecondaryText) }
        }
    )
}

// ── Resume card ──────────────────────────────────────────────────────────────

@Composable
private fun ResumeManageCard(
    filename: String,
    subtitle: String,
    isGenerated: Boolean,
    isDeleting: Boolean,
    onSearch: () -> Unit,
    onPreview: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FoundationSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SecondaryText, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerated) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryText)
                } else {
                    Text("📄", color = PrimaryText)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(filename, style = MaterialTheme.typography.bodyLarge, color = PrimaryText)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }

            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = UiInteractive)
                }
                IconButton(onClick = onPreview) {
                    Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = UiInteractive)
                }
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = UiInteractive)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentAlert)
                }
            }
        }
    }
}