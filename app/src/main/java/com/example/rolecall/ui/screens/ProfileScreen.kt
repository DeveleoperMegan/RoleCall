package com.example.rolecall.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.AuthViewModel
import com.example.rolecall.ui.viewmodel.ProfileViewModel
import java.io.File

@Composable
fun ProfileScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val profile = profileViewModel.profile
    val isLoading = profileViewModel.isLoading
    val errorMessage = profileViewModel.errorMessage
    val savedCount by profileViewModel.savedJobsCount.collectAsState()
    val appliedCount by profileViewModel.appliedJobsCount.collectAsState()
    val matchCount by profileViewModel.matchHistoryCount.collectAsState()

    val context = LocalContext.current

    // Load the profile once when the screen appears
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    // Avatar picker
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File(context.cacheDir, fileName)
            inputStream?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            profileViewModel.uploadAvatar(tempFile, "image/jpeg")
        }
    }

    LaunchedEffect(authState.isLoggedIn) {
        if (!authState.isLoggedIn) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    RoleCallScaffold(navController = navController, title = "Profile", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar (clickable to change)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(FoundationSurface)
                    .clickable { avatarPickerLauncher.launch(arrayOf("image/*")) },
                contentAlignment = Alignment.Center
            ) {
                if (profile?.avatarUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Profile Picture",
                        tint = SecondaryText,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Delete avatar option if one exists
            if (profile?.avatarUrl?.isNotBlank() == true) {
                TextButton(onClick = { profileViewModel.deleteAvatar() }) {
                    Text("Remove Photo", color = AccentAlert)
                }
            } else {
                Text(
                    "Tap to add a photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    profile?.fullName ?: "User",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryText
                )
                if (!profile?.title.isNullOrBlank()) {
                    Text(
                        profile.title!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    profile?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = AccentAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FoundationSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stats", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(savedCount.toString(), "Saved Jobs")
                        StatItem(matchCount.toString(), "Matches")
                        StatItem(appliedCount.toString(), "Applied")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FoundationSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { navController.navigate(Routes.EDIT_PROFILE) }) {
                        Text("Edit Profile", color = UiInteractive)
                    }
                    TextButton(onClick = { navController.navigate(Routes.CHANGE_PASSWORD) }) {
                        Text("Change Password", color = UiInteractive)
                    }
                    TextButton(onClick = { navController.navigate(Routes.NOTIFICATION_SETTINGS) }) {
                        Text("Notifications", color = UiInteractive)
                    }
                    TextButton(onClick = { navController.navigate(Routes.MY_RESUMES) }) {
                        Text("My Resumes", color = UiInteractive)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { authViewModel.logOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAlert)
            ) {
                Text("Log Out")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = PrimaryText)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
    }
}