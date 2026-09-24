package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.local.dao.GeneratedResumeDao
import com.example.rolecall.ui.components.PdfPreviewView
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.AccentAlert
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class LocalPreviewState {
    data object Loading : LocalPreviewState()
    data class Success(val path: String) : LocalPreviewState()
    data class Error(val message: String) : LocalPreviewState()
}

@HiltViewModel
class LocalResumePreviewViewModel @Inject constructor(
    private val dao: GeneratedResumeDao
) : ViewModel() {
    var state by mutableStateOf<LocalPreviewState>(LocalPreviewState.Loading)
        private set

    fun load(resumeId: String) {
        viewModelScope.launch {
            val entity = withContext(Dispatchers.IO) { dao.getById(resumeId) }
            state = if (entity != null) LocalPreviewState.Success(entity.localPath)
            else LocalPreviewState.Error("Resume not found")
        }
    }
}

@Composable
fun LocalResumePreviewScreen(navController: NavController, resumeId: String) {
    val viewModel: LocalResumePreviewViewModel = hiltViewModel()
    LaunchedEffect(resumeId) { viewModel.load(resumeId) }

    RoleCallScaffold(navController = navController, title = "Preview", showSearchBar = false) { modifier ->
        when (val s = viewModel.state) {
            is LocalPreviewState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is LocalPreviewState.Error   -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(s.message, color = AccentAlert) }
            is LocalPreviewState.Success -> PdfPreviewView(filePath = s.path, modifier = modifier)
        }
    }
}
