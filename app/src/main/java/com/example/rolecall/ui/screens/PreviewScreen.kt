package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rolecall.ui.components.PdfPreviewView
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.AccentAlert
import com.example.rolecall.ui.viewmodel.PreviewUiState
import com.example.rolecall.ui.viewmodel.PreviewViewModel

@Composable
fun PreviewScreen(navController: NavController, resumeId: String) {
    val viewModel: PreviewViewModel = hiltViewModel()
    val context = LocalContext.current

    LaunchedEffect(resumeId) {
        viewModel.loadPreview(resumeId, context.cacheDir)
    }

    RoleCallScaffold(
        navController = navController,
        title = "File Preview",
        showSearchBar = false
    ) { modifier ->
        when (val state = viewModel.uiState) {
            is PreviewUiState.Loading -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PreviewUiState.Error -> {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = AccentAlert)
                }
            }
            is PreviewUiState.Success -> {
                if (state.fileType.lowercase().contains("pdf")) {
                    PdfPreviewView(filePath = state.filePath, modifier = modifier)
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(state.filePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Resume preview",
                        modifier = modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}