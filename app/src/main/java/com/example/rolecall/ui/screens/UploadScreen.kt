package com.example.rolecall.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.remote.ResumeApiService
import com.example.rolecall.data.remote.UploadResponse
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.PrimaryText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val resumeApiService: ResumeApiService
) : ViewModel() {

    var uploadState by mutableStateOf<UploadUiState>(UploadUiState.Idle)
        private set

    fun uploadPdf(file: File) {
        viewModelScope.launch {
            uploadState = UploadUiState.Loading
            try {
                val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                val response = resumeApiService.uploadResume(part)

                if (response.isSuccessful) {
                    val body = response.body()
                    uploadState = UploadUiState.Success(body?.message ?: "Upload successful!")
                } else {
                    uploadState = UploadUiState.Error("Upload failed: ${response.code()}")
                }
            } catch (e: Exception) {
                uploadState = UploadUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        uploadState = UploadUiState.Idle
    }
}

sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val message: String) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

@Composable
fun UploadScreen(navController: NavController) {
    val viewModel: UploadViewModel = hiltViewModel()
    val context = LocalContext.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            selectedFileName = fileName

            // Copy to cache for upload
            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, fileName ?: "resume.pdf")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            selectedFile = tempFile
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = "RoleCall",
        showSearchBar = false
    ) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome to RoleCall",
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Upload PDF button
            Button(onClick = {
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
            }) {
                Text("Upload PDF")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Take Photo button (placeholder)
            Button(onClick = {
                selectedFileName = "resume_photo.jpg"
            }) {
                Text("Take Photo")
            }

            selectedFileName?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Selected: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryText
                )
            }

            // Upload to backend
            if (selectedFile != null && viewModel.uploadState !is UploadUiState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.uploadPdf(selectedFile!!) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.rolecall.ui.theme.AccentSuccess
                    )
                ) {
                    Text("Upload to Server")
                }
            }

            // Show upload state
            when (val state = viewModel.uploadState) {
                is UploadUiState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Text("Uploading...", color = PrimaryText)
                }
                is UploadUiState.Success -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, color = com.example.rolecall.ui.theme.AccentSuccess)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate(Routes.RESULTS)
                    }) {
                        Text("See Results")
                    }
                }
                is UploadUiState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, color = com.example.rolecall.ui.theme.AccentAlert)
                }
                is UploadUiState.Idle -> { /* Nothing */ }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(onClick = { navController.navigate(Routes.RESULTS) }) {
                Text("See Results (mock)")
            }
        }
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name ?: "resume.pdf"
}