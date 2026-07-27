package com.example.rolecall.ui.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// ──────────────────────────────────────────────────────────────────────────────
// JSON response models (must match backend exactly)
// ──────────────────────────────────────────────────────────────────────────────

data class UploadJsonResponse(
    @SerializedName("resume_id") val resumeId: String,
    val filename: String,
    @SerializedName("text_length") val textLength: Int
)

data class SearchJsonResponse(
    @SerializedName("resume_id") val resumeId: String?,
    @SerializedName("matches") val matches: List<MatchJsonItem>?
)

data class MatchJsonItem(
    val id: String,
    @SerializedName("job_id") val jobId: Long?,
    @SerializedName("company_name") val companyName: String?,
    val title: String,
    val description: String,
    @SerializedName("max_salary") val maxSalary: Double?,
    @SerializedName("min_salary") val minSalary: Double?,
    @SerializedName("post_date") val postDate: String?,
    @SerializedName("post_url") val postUrl: String?,
    @SerializedName("expiration_date") val expirationDate: String?,
    val similarity: Double
)

// ──────────────────────────────────────────────────────────────────────────────
// UploadViewModel (extended with saved resumes support)
// ──────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    var uploadState by mutableStateOf<UploadUiState>(UploadUiState.Idle)
        private set
    var matchState by mutableStateOf<MatchUiState>(MatchUiState.Idle)
        private set

    // State for previously uploaded resumes
    var savedResumes by mutableStateOf<List<ResumeItem>>(emptyList())
        private set
    var isLoadingResumes by mutableStateOf(false)
        private set

    private val gson = Gson()

    // Load the list of resumes already stored on the server
    fun loadSavedResumes() {
        viewModelScope.launch {
            isLoadingResumes = true
            val list = withContext(Dispatchers.IO) {
                fastAPIRepository.getResumes()
            }
            savedResumes = list ?: emptyList()
            isLoadingResumes = false
        }
    }

    // Search using an existing resume ID (no upload needed)
    fun searchExistingResume(resumeId: String, navController: NavController) {
        viewModelScope.launch {
            matchState = MatchUiState.Loading
            val searchJson = withContext(Dispatchers.IO) {
                fastAPIRepository.searchJobs(resumeId)
            }
            Log.i("UPLOAD_DEBUG", "Search JSON from existing resume: $searchJson")
            if (searchJson == null) {
                matchState = MatchUiState.Error("Search failed")
                return@launch
            }
            val searchResponse = try {
                gson.fromJson(searchJson, SearchJsonResponse::class.java)
            } catch (e: Exception) {
                Log.e("UPLOAD_DEBUG", "Parse error: ${e.message}")
                matchState = MatchUiState.Error("Failed to parse results")
                return@launch
            }
            val matchList = searchResponse.matches ?: emptyList()
            Log.i("UPLOAD_DEBUG", "Final match list size: ${matchList.size}")
            val jobs = matchList.map { match ->
                JobItem(
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
            }
            matchState = MatchUiState.Success(jobs.size)
            MatchResultsHolder.setResults(jobs)
            navController.navigate(Routes.RESULTS) {
                popUpTo(Routes.UPLOAD) { inclusive = false }
            }
        }
    }

    // Full upload → search pipeline (unchanged from before)
    fun uploadAndSearch(file: File, mimeType: String, navController: NavController) {
        viewModelScope.launch {
            uploadState = UploadUiState.Loading

            val uploadJson = withContext(Dispatchers.IO) {
                fastAPIRepository.uploadResume(file, mimeType)
            }
            if (uploadJson == null) {
                uploadState = UploadUiState.Error("Upload failed")
                return@launch
            }

            val uploadResponse = gson.fromJson(uploadJson, UploadJsonResponse::class.java)
            uploadState = UploadUiState.Success(uploadResponse.filename, uploadResponse.textLength)

            matchState = MatchUiState.Loading
            val searchJson = withContext(Dispatchers.IO) {
                fastAPIRepository.searchJobs(uploadResponse.resumeId)
            }
            Log.i("UPLOAD_DEBUG", "Search JSON: $searchJson")
            if (searchJson == null) {
                matchState = MatchUiState.Error("Search failed")
                return@launch
            }

            val searchResponse = try {
                gson.fromJson(searchJson, SearchJsonResponse::class.java)
            } catch (e: Exception) {
                Log.e("UPLOAD_DEBUG", "Parse error: ${e.message}")
                matchState = MatchUiState.Error("Failed to parse results")
                return@launch
            }

            val matchList = searchResponse.matches ?: emptyList()
            Log.i("UPLOAD_DEBUG", "Final match list size: ${matchList.size}")
            val jobs = matchList.map { match ->
                Log.i("UPLOAD_DEBUG", "Match: ${match.title}, similarity: ${match.similarity}")
                JobItem(
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
            }
            matchState = MatchUiState.Success(jobs.size)
            MatchResultsHolder.setResults(jobs)
            navController.navigate(Routes.RESULTS) {
                popUpTo(Routes.UPLOAD) { inclusive = false }
            }
        }
    }
}

// ── UI state classes ─────────────────────────────────────────────────────────
sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val filename: String, val textLength: Int) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

sealed class MatchUiState {
    data object Idle : MatchUiState()
    data object Loading : MatchUiState()
    data class Success(val matchCount: Int) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

// ── UploadScreen composable (complete with camera preview) ──────────────────

@Composable
fun UploadScreen(navController: NavController) {
    val viewModel: UploadViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val cameraController = remember { LifecycleCameraController(context) }

    // Load the list of previously uploaded resumes when the screen appears
    LaunchedEffect(Unit) {
        viewModel.loadSavedResumes()
    }

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
        title = "RoleCall",
        showSearchBar = false
    ) { modifier ->
        if (showCamera) {
            // ── Camera Preview ───────────────────────────────────────────
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
                        TextButton(onClick = {
                            showCamera = false
                            cameraController.unbind()
                        }) {
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
                        }) {
                            Text("Capture")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                }
            }
        } else {
            // ── Upload Form (with saved resumes section) ─────────────────
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
                Spacer(modifier = Modifier.height(16.dp))

                // ── Previously Uploaded Resumes ───────────────────────────
                if (viewModel.isLoadingResumes) {
                    CircularProgressIndicator()
                } else if (viewModel.savedResumes.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Uploaded Resumes",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryText
                        )
                        TextButton(onClick = { navController.navigate(Routes.RESUME_LIST) }) {
                            Text("View All", color = UiInteractive)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .fillMaxWidth()
                    ) {
                        items(viewModel.savedResumes) { resume ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.searchExistingResume(resume.id, navController)
                                    },
                                colors = CardDefaults.cardColors(containerColor = FoundationSurface),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        resume.filename,
                                        color = PrimaryText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Uploaded: ${resume.createdAt.take(10)}",
                                        color = SecondaryText,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Upload New Resume Buttons ────────────────────────────
                Button(onClick = {
                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                }) {
                    Text("Upload New PDF")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Take Photo")
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    imagePickerLauncher.launch(arrayOf("image/*"))
                }) {
                    Text("Choose Photo (PDF works best)")
                }

                // ── Selected file name & upload button ────────────────────
                selectedFileName?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Selected: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )
                }

                if (selectedFile != null &&
                    viewModel.uploadState !is UploadUiState.Loading &&
                    viewModel.matchState !is MatchUiState.Loading
                ) {
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
                            viewModel.uploadAndSearch(selectedFile!!, mimeType, navController)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                    ) {
                        Text("Upload and Find Jobs")
                    }
                }

                // ── Upload state display ───────────────────────────────────
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

                // ── Match state display ────────────────────────────────────
                when (val state = viewModel.matchState) {
                    is MatchUiState.Loading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                        Text("Finding matches...", color = PrimaryText)
                    }
                    is MatchUiState.Success -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Found ${state.matchCount} matches!", color = AccentSuccess)
                    }
                    is MatchUiState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = AccentAlert)
                    }
                    is MatchUiState.Idle -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Helper: extract filename from content URI ────────────────────────────────
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