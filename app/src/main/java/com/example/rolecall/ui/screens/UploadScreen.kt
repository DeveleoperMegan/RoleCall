package com.example.rolecall.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rolecall.R
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.theme.FoundationDark
import com.example.rolecall.ui.theme.SecondaryText

@Composable
fun UploadScreen(navController: NavController) {
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryText),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = FoundationDark,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rolecall_logo),
                    contentDescription = "RoleCall Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "RoleCall",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = {
                    selectedFileName = "resume.pdf"
                }) {
                    Text("Upload PDF/DOCX")
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(onClick = { navController.navigate(Routes.RESULTS) }) {
                    Text("See Results (mock)")
                }
            }
        }
    }
}