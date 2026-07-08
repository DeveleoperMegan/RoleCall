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

@Composable
fun EditProfileScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    RoleCallScaffold(navController = navController, title = "Edit Profile", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Update Your Info",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    disabledTextColor = SecondaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Email cannot be changed",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Save profile changes locally or to backend
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (saved) AccentSuccess else UiInteractive
                )
            ) {
                Text(if (saved) "Saved!" else "Save Changes")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel", color = SecondaryText)
            }
        }
    }
}

