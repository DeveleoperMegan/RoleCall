package com.example.rolecall.ui.screens

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.PreviewUiState
import com.example.rolecall.ui.viewmodel.PreviewViewModel
import java.io.File
import androidx.compose.ui.graphics.Color

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
                    PdfPreview(state.filePath, modifier)
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


@Composable
private fun PdfPreview(filePath: String, modifier: Modifier = Modifier) {
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load the first page
    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    error = "File not found"
                    return@withContext
                }
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                pageCount = pdfRenderer.pageCount
                if (pageCount > 0) {
                    val page = pdfRenderer.openPage(0)
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pageBitmap = bitmap
                    page.close()
                }
                pdfRenderer.close()
                fileDescriptor.close()
            } catch (e: Exception) {
                error = "Can't open PDF"
            }
        }
    }

    // Re‑render when page changes
    LaunchedEffect(currentPage) {
        if (currentPage in 0 until pageCount) {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(filePath)
                    val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fd)
                    if (currentPage < renderer.pageCount) {
                        val page = renderer.openPage(currentPage)
                        val bitmap = android.graphics.Bitmap.createBitmap(
                            page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pageBitmap = bitmap
                        page.close()
                    }
                    renderer.close()
                    fd.close()
                } catch (e: Exception) {
                    error = "Can't render page ${currentPage + 1}"
                }
            }
        }
    }

    if (error != null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error!!, color = AccentAlert)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation controls (on the dark background)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) { Text("Previous", color = PrimaryText) }

                Text("Page ${currentPage + 1} of $pageCount", color = PrimaryText)

                TextButton(
                    onClick = { if (currentPage < pageCount - 1) currentPage++ },
                    enabled = currentPage < pageCount - 1
                ) { Text("Next", color = PrimaryText) }
            }

            // White card containing the PDF page
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (pageBitmap != null) {
                        Image(
                            bitmap = pageBitmap!!.asImageBitmap(),
                            contentDescription = "PDF page ${currentPage + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

