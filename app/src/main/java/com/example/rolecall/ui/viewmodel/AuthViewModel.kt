package com.example.rolecall.ui.viewmodel

import android.app.Activity
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

/**
 * UI state for the authentication screens.
 *
 * @param isLoggedIn true when a valid JWT is present in TokenManager
 * @param isLoading  true while a network auth call is in flight
 * @param errorMessage human-readable error to surface in the UI, or null
 */
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel that owns the authentication flow.
 *
 * Delegates all Supabase work to [AuthRepository] and persists the resulting
 * JWT + refresh token via [TokenManager]. The UI observes [uiState] and reacts
 * to isLoggedIn / isLoading / errorMessage changes.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // On app start, check whether a JWT is already stored.
        // If so, treat the user as logged in without hitting the network.
        val existingToken = tokenManager.getJWT()
        if (existingToken != null) {
            _uiState.update { it.copy(isLoggedIn = true) }
        }
    }

    /**
     * Register a new user with email + password.
     * On success, the returned JWT is persisted and isLoggedIn flips to true.
     */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            // 1. Flag loading and clear any previous error
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 2. Call Supabase sign-up via the repository
            val result = AuthRepository.signUpWithEmail(email, password, tokenManager)

            // 3. Update state based on the outcome
            if (result != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sign up failed. Try again.")
                }
            }
        }
    }

    /**
     * Authenticate an existing user with email + password.
     * On success, the returned JWT is persisted and isLoggedIn flips to true.
     */
    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            // 1. Flag loading and clear any previous error
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 2. Call Supabase sign-in via the repository
            val result = AuthRepository.loginWithEmail(email, password, tokenManager)

            // 3. Update state based on the outcome
            if (result != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Login failed. Check your credentials.")
                }
            }
        }
    }

    /**
     * Authenticate via Google Sign-In.
     *
     * @param activity the hosting Activity — required by the Google credential
     *                 client to launch the consent intent. Nullable so the
     *                 caller doesn't have to force-unwrap in Compose.
     *
     * Flow: request an ID token from Google → exchange it with Supabase →
     * persist the resulting JWT + refresh token.
     */
    fun signInWithGoogle(activity: Activity?) {
        // Guard: without an Activity we can't open the Google consent screen.
        if (activity == null) {
            _uiState.update { it.copy(errorMessage = "Unable to start Google sign-in.") }
            return
        }
        viewModelScope.launch {
            // 1. Flag loading and clear any previous error
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 2. Launch Google consent + exchange ID token with Supabase
            val result = AuthRepository.loginWithGoogle(activity, tokenManager)

            // 3. Update state based on the outcome
            if (result != null) {
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Google sign-in failed. Try again.")
                }
            }
        }
    }

    /**
     * Sign the user out of Supabase and clear any locally stored tokens.
     * Failures during sign-out are swallowed by the repository; local tokens
     * are cleared regardless so the app returns to a logged-out state.
     */
    fun logOut() {
        viewModelScope.launch {
            AuthRepository.signOut(tokenManager)
            _uiState.update { it.copy(isLoggedIn = false) }
        }
    }

    /**
     * Clear the current error message. Call this when the user dismisses
     * an error banner or starts typing in the login form again.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}