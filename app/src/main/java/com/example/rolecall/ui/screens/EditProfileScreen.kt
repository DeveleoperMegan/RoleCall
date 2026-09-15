package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import com.example.rolecall.ui.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(navController: NavController) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profile = viewModel.profile

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    // Pre-fill when profile loads
    LaunchedEffect(profile) {
        profile?.let {
            firstName = it.firstName ?: ""
            lastName = it.lastName ?: ""
            title = it.title ?: ""
        }
    }

    // Load profile if not already loaded
    LaunchedEffect(Unit) {
        if (profile == null) viewModel.loadProfile()
    }

    RoleCallScaffold(navController = navController, title = "Edit Profile", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Update Your Info", style = MaterialTheme.typography.headlineSmall, color = PrimaryText)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it; saved = false },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it; saved = false },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; saved = false },
                label = { Text("Professional Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(
                        firstName.ifBlank { null },
                        lastName.ifBlank { null },
                        title.ifBlank { null }
                    )
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (saved) AccentSuccess else UiInteractive
                )
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryText)
                } else {
                    Text(if (saved) "Saved!" else "Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel", color = SecondaryText)
            }
        }
    }
}