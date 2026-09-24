package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.ProviderButtonRow
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.components.findActivity
import com.example.rolecall.ui.theme.PrimaryText
import com.example.rolecall.ui.theme.SecondaryText
import com.example.rolecall.ui.theme.UiInteractive
import com.example.rolecall.ui.viewmodel.AuthViewModel

@Composable
fun SignupScreen(navController: NavController) {
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current.findActivity()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SIGNUP) { inclusive = true }
            }
        }
    }

    RoleCallScaffold(navController = navController, title = "Sign Up", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = PrimaryText)
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Passwords do not match")
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

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signUp(email, password) },
                enabled = !uiState.isLoading &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        password == confirmPassword,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PrimaryText
                    )
                } else {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google sign-up — same call as LoginScreen; Supabase creates the
            // user automatically if no account exists for that Google email.
            ProviderButtonRow(
                onGoogleClick = { viewModel.signInWithGoogle(activity) },
                onGithubClick = { viewModel.signInWithGithub() },
                onMicrosoftClick = { viewModel.signInWithMicrosoft() },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                Text("Already have an account? Log In", color = SecondaryText)
            }
            TextButton(onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Text("Skip for now, go to Home", color = SecondaryText)
            }
        }
    }
}