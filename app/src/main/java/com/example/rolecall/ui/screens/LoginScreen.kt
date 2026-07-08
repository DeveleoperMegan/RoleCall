package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.PrimaryText
import com.example.rolecall.ui.theme.SecondaryText
import com.example.rolecall.ui.theme.UiInteractive

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate(Routes.UPLOAD) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    RoleCallScaffold(navController = navController, title = "Log In", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineMedium, color = PrimaryText)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
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
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show", color = SecondaryText)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = PrimaryText,
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = SecondaryText
                )
            )

            // Forgot Password link
            TextButton(
                onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = SecondaryText)
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.logIn(email, password) },
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryText)
                } else {
                    Text("Log In")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Routes.SIGNUP) }) {
                Text("Don't have an account? Sign Up", color = SecondaryText)
            }
        }
    }
}