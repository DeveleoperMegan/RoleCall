package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.ui.components.PdfPreviewView
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.AccentAlert
import com.example.rolecall.ui.theme.PrimaryText
import com.example.rolecall.ui.viewmodel.GeneratedPreviewState
import com.example.rolecall.ui.viewmodel.GeneratedResumePreviewViewModel

@Composable
fun GeneratedResumePreviewScreen(navController: NavController, resumeId: String) {
    val viewModel: GeneratedResumePreviewViewModel = hiltViewModel()

    LaunchedEffect(resumeId) {
        viewModel.load(resumeId)
    }

    RoleCallScaffold(
        navController = navController,
        title = "Your New Resume",
        showSearchBar = false
    ) { modifier ->
        when (val s = viewModel.state) {
            is GeneratedPreviewState.Loading -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is GeneratedPreviewState.Error -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = AccentAlert)
                }
            }
            is GeneratedPreviewState.Success -> {
                Column(modifier = modifier.fillMaxSize()) {
                    Text(
                        "Here's your ATS-optimized resume:",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText,
                        modifier = Modifier.padding(16.dp)
                    )
                    PdfPreviewView(filePath = s.filePath, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Back to Builder")
                    }
                }
            }
        }
    }
}

