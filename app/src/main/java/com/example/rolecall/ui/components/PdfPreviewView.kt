package com.example.rolecall.ui.components

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.rolecall.ui.theme.PrimaryText
import com.example.rolecall.ui.theme.AccentAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfPreviewView(filePath: String, modifier: Modifier = Modifier) {
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    error = "File not found"
                    return@withContext
                }
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                pageCount = renderer.pageCount
                if (pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bmp = android.graphics.Bitmap.createBitmap(
                        page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888
                    )
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pageBitmap = bmp
                    page.close()
                }
                renderer.close()
                fd.close()
            } catch (e: Exception) {
                error = "Can't open PDF"
            }
        }
    }

    LaunchedEffect(currentPage) {
        if (currentPage in 0 until pageCount) {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(filePath)
                    val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fd)
                    if (currentPage < renderer.pageCount) {
                        val page = renderer.openPage(currentPage)
                        val bmp = android.graphics.Bitmap.createBitmap(
                            page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888
                        )
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        pageBitmap = bmp
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
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pageCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
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
            }

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
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
