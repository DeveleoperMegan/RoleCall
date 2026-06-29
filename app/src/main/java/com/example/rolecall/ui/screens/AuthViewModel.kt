package com.example.rolecall.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.network.AuthRepository
import com.example.rolecall.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if user is already logged in (has a stored JWT)
        val existingToken = tokenManager.getJWT()
        if (existingToken != null) {
            _uiState.update { it.copy(isLoggedIn = true) }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = AuthRepository.signUpWithEmail(email, password, tokenManager)
            if (result != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sign up failed. Try again.")
                }
            }
        }
    }

    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = AuthRepository.loginWithEmail(email, password, tokenManager)
            if (result != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Login failed. Check your credentials.")
                }
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            AuthRepository.signOut(tokenManager)
            _uiState.update { it.copy(isLoggedIn = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}