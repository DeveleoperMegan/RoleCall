package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.network.AuthRepository
import com.example.rolecall.network.TokenManager
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                AuthRepository.sendPasswordResetEmail(email, tokenManager)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to send reset email"
                    )
                }
            }
        }
    }

    fun clearState() {
        _uiState.update { ForgotPasswordUiState() }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }

    RoleCallScaffold(navController = navController, title = "Reset Password", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isSuccess) {
                Text(
                    "Check Your Email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "If an account exists for $email, we've sent a password reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
                ) {
                    Text("Back to Login")
                }
            } else {
                Text(
                    "Forgot Password?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Enter your email and we'll send you a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
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

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.errorMessage!!, color = AccentAlert)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.sendResetEmail(email) },
                    enabled = !uiState.isLoading && email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryText)
                    } else {
                        Text("Send Reset Link")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Back to Login", color = SecondaryText)
                }
            }
        }
    }
}