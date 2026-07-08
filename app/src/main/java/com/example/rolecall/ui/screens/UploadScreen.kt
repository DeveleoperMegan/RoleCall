package com.example.rolecall.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.remote.MatchResponse
import com.example.rolecall.data.remote.UploadResponse
import com.example.rolecall.data.repository.ResumeRepository
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository
) : ViewModel() {

    var uploadState by mutableStateOf<UploadUiState>(UploadUiState.Idle)
        private set

    var matchState by mutableStateOf<MatchUiState>(MatchUiState.Idle)
        private set

    private var extractedText: String? = null

    fun uploadFile(file: File, mimeType: String = "application/pdf") {
        viewModelScope.launch {
            uploadState = UploadUiState.Loading
            val result = resumeRepository.uploadFile(file, mimeType)
            result.fold(
                onSuccess = { response ->
                    extractedText = response.extractedText
                    uploadState = UploadUiState.Success(response)
                },
                onFailure = { error ->
                    uploadState = UploadUiState.Error(error.localizedMessage ?: "Upload failed")
                }
            )
        }
    }

    fun matchAndNavigate(navController: NavController) {
        val text = extractedText ?: return
        viewModelScope.launch {
            matchState = MatchUiState.Loading
            val result = resumeRepository.matchResume(text)
            result.fold(
                onSuccess = { response ->
                    matchState = MatchUiState.Success(response)
                    navController.navigate(Routes.RESULTS) {
                        popUpTo(Routes.UPLOAD) { inclusive = false }
                    }
                },
                onFailure = { error ->
                    matchState = MatchUiState.Error(error.localizedMessage ?: "Match failed")
                }
            )
        }
    }

    fun resetState() {
        uploadState = UploadUiState.Idle
        matchState = MatchUiState.Idle
        extractedText = null
    }
}

sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val response: UploadResponse) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

sealed class MatchUiState {
    data object Idle : MatchUiState()
    data object Loading : MatchUiState()
    data class Success(val response: MatchResponse) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

@Composable
fun UploadScreen(navController: NavController) {
    val viewModel: UploadViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    // Camera controller
    val cameraController = remember { LifecycleCameraController(context) }

    // PDF picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            selectedFileName = fileName

            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, fileName ?: "resume.pdf")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            selectedFile = tempFile
            showCamera = false
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = "RoleCall",
        showSearchBar = false
    ) { modifier ->
        if (showCamera) {
            // Camera preview screen
            Column(modifier = modifier.fillMaxSize()) {
                // Camera preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                            cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // Capture button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FoundationDark,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel
                        TextButton(
                            onClick = {
                                showCamera = false
                                cameraController.unbind()
                            }
                        ) {
                            Text("Cancel", color = AccentAlert)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Capture
                        Button(
                            onClick = {
                                val photoFile = File(
                                    context.cacheDir,
                                    "resume_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                                )
                                cameraController.takePicture(
                                    ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                                    context.mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            selectedFileName = photoFile.name
                                            selectedFile = photoFile
                                            showCamera = false
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            selectedFileName = "Capture failed"
                                        }
                                    }
                                )
                            }
                        ) {
                            Text("Capture")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Spacer for symmetry
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
        } else {
            // Upload screen
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

                Button(onClick = {
                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                }) {
                    Text("Upload PDF")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    cameraController.bindToLifecycle(lifecycleOwner)
                    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    showCamera = true
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

                if (selectedFile != null && viewModel.uploadState !is UploadUiState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val mimeType = if (selectedFile?.name?.endsWith(".jpg") == true ||
                                selectedFile?.name?.endsWith(".jpeg") == true
                            ) "image/jpeg" else "application/pdf"
                            viewModel.uploadFile(selectedFile!!, mimeType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                    ) {
                        Text("Upload to Server")
                    }
                }

                when (val state = viewModel.uploadState) {
                    is UploadUiState.Loading -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Text("Uploading...", color = PrimaryText)
                    }
                    is UploadUiState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.response.message, color = AccentSuccess)

                        if (viewModel.matchState !is MatchUiState.Loading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.matchAndNavigate(navController) },
                                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
                            ) {
                                Text("Find Matching Jobs")
                            }
                        }
                    }
                    is UploadUiState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = AccentAlert)
                    }
                    is UploadUiState.Idle -> {}
                }

                when (val state = viewModel.matchState) {
                    is MatchUiState.Loading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                        Text("Matching...", color = PrimaryText)
                    }
                    is MatchUiState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = AccentAlert)
                    }
                    is MatchUiState.Success -> {}
                    is MatchUiState.Idle -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))
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

