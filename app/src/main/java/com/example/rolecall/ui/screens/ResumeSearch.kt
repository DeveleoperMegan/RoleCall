package com.example.rolecall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.ResumeListViewModel

@Composable
fun ResumeSearchScreen(navController: NavController) {
    val viewModel: ResumeListViewModel = hiltViewModel()
    var searchQuery by remember { mutableStateOf("") }

    fun submitTermSearch() {
        if (searchQuery.isNotBlank()) {
            navController.navigate("web_search_animation/$searchQuery")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadResumes()
    }

    RoleCallScaffold(
        navController = navController,
        title = "Search Jobs",
        showSearchBar = false  // we use our own search bar here
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar for term-based search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by job title or keyword", color = SecondaryText) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = SecondaryText)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitTermSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = Border,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = UiInteractive
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { submitTermSearch() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
            ) {
                Text("Search Jobs")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Or use an uploaded resume",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.resumes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No resumes uploaded yet.", color = SecondaryText)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { navController.navigate(Routes.UPLOAD) }) {
                    Text("Upload Resume")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.resumes) { resume ->
                        ResumeCard(
                            resume = resume,
                            onClick = {
                                navController.navigate("matching_animation/${resume.id}")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { navController.navigate(Routes.UPLOAD) }) {
                    Text("Upload New Resume")
                }
            }
        }
    }
}

@Composable
private fun ResumeCard(
    resume: ResumeItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = FoundationSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SecondaryText, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📄", color = PrimaryText)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = resume.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryText
                )
                Text(
                    text = "Uploaded: ${resume.createdAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }
    }
}