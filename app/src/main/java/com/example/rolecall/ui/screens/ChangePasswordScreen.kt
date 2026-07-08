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

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                AuthRepository.changePassword(newPassword)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to change password"
                    )
                }
            }
        }
    }

    fun clearState() {
        _uiState.update { ChangePasswordUiState() }
    }
}

@Composable
fun ChangePasswordScreen(navController: NavController) {
    val viewModel: ChangePasswordViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    RoleCallScaffold(navController = navController, title = "Change Password", showSearchBar = false) { modifier ->
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isSuccess) {
                Text(
                    "Password Changed!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Your password has been updated successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
                ) {
                    Text("Back to Profile")
                }
            } else {
                Text(
                    "Change Password",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
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
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
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
                    Text(uiState.errorMessage!!, color = AccentAlert)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.changePassword(newPassword) },
                    enabled = !uiState.isLoading &&
                            newPassword.isNotBlank() &&
                            newPassword == confirmPassword &&
                            newPassword.length >= 6,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryText)
                    } else {
                        Text("Update Password")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel", color = SecondaryText)
                }
            }
        }
    }
}
