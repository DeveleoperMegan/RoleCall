package com.example.rolecall.network

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {

    // Register user with email and password
    suspend fun signUpWithEmail(emailInput: String, passwordInput: String, tokenManager: TokenManager): String? {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                val jwt = SupabaseClient.client.auth.currentAccessTokenOrNull()
                val refreshToken = SupabaseClient.client.auth.currentSessionOrNull()?.refreshToken

                if (jwt != null) {
                    tokenManager.saveJWT(jwt)
                    if (refreshToken != null) tokenManager.saveRefreshToken(refreshToken)
                }
                jwt
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Authenticate user with email and password
    suspend fun loginWithEmail(emailInput: String, passwordInput: String, tokenManager: TokenManager): String? {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                val jwt = SupabaseClient.client.auth.currentAccessTokenOrNull()
                val refreshToken = SupabaseClient.client.auth.currentSessionOrNull()?.refreshToken

                if (jwt != null) {
                    tokenManager.saveJWT(jwt)
                    if (refreshToken != null) tokenManager.saveRefreshToken(refreshToken)
                }
                jwt
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(emailInput: String) {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(emailInput)
                Log.i("SUPABASE", "Password reset email sent to $emailInput")
            } catch (e: Exception) {
                Log.e("SUPABASE", "Password reset error", e)
                throw e
            }
        }
    }

    // Change password for logged-in user
    suspend fun changePassword(newPassword: String) {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                Log.i("SUPABASE", "Password changed successfully")
            } catch (e: Exception) {
                Log.e("SUPABASE", "Password change error", e)
                throw e
            }
        }
    }

    // Refresh the access token using the stored refresh token
    suspend fun refreshAccessToken(tokenManager: TokenManager): String? {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.refreshCurrentSession()
                val newToken = SupabaseClient.client.auth.currentAccessTokenOrNull()
                if (newToken != null) {
                    tokenManager.saveJWT(newToken)
                }
                newToken
            } catch (e: Exception) {
                Log.e("SUPABASE", "Refresh token error", e)
                null
            }
        }
    }

    // Sign out
    suspend fun signOut(tokenManager: TokenManager) {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signOut()
                Log.i("SUPABASE", "Successful Logout")
            } catch (e: Exception) {
                Log.e("SUPABASE", "Sign Out Error", e)
            } finally {
                tokenManager.clearTokens()
            }
        }
    }
}
