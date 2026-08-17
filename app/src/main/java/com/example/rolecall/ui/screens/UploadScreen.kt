package com.example.rolecall.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.UploadUiState
import com.example.rolecall.ui.viewmodel.UploadViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UploadScreen(navController: NavController) {
    val viewModel: UploadViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val cameraController = remember { LifecycleCameraController(context) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraController.bindToLifecycle(lifecycleOwner)
            cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            showCamera = true
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            selectedFileName = fileName
            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, fileName ?: "resume.pdf")
            inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
            selectedFile = tempFile
            showCamera = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            selectedFileName = fileName
            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, fileName ?: "resume_photo.jpg")
            inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
            selectedFile = tempFile
            showCamera = false
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = "Upload Resume",
        showSearchBar = false
    ) { modifier ->
        if (showCamera) {
            Column(modifier = modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                            cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Surface(modifier = Modifier.fillMaxWidth(), color = FoundationDark, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCamera = false; cameraController.unbind() }) {
                            Text("Cancel", color = AccentAlert)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
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
                                        showCamera = false
                                    }
                                }
                            )
                        }) { Text("Capture") }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Upload Your Resume",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                    Text("Upload PDF")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Take Photo")
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }) {
                    Text("Choose from Gallery")
                }

                selectedFileName?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selected: $it", style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
                }

                if (selectedFile != null && viewModel.uploadState !is UploadUiState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val mimeType = when {
                                selectedFile?.name?.endsWith(".jpg") == true -> "image/jpg"
                                selectedFile?.name?.endsWith(".jpeg") == true -> "image/jpg"
                                selectedFile?.name?.endsWith(".png") == true -> "image/png"
                                selectedFile?.name?.endsWith(".webp") == true -> "image/webp"
                                selectedFile?.name?.endsWith(".txt") == true -> "text/plain"
                                else -> "application/pdf"
                            }
                            viewModel.uploadAndNavigateToAnimation(selectedFile!!, mimeType, navController)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                    ) {
                        Text("Upload and Find Jobs")
                    }
                }

                when (val state = viewModel.uploadState) {
                    is UploadUiState.Loading -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Text("Uploading...", color = PrimaryText)
                    }
                    is UploadUiState.Success -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uploaded! (${state.textLength} chars)", color = AccentSuccess)
                    }
                    is UploadUiState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, color = AccentAlert)
                    }
                    is UploadUiState.Idle -> {}
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
    return name ?: "document"
}