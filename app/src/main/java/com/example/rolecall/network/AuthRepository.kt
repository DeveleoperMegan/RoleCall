package com.example.rolecall.network

import android.app.Activity
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.auth.providers.builtin.IDToken

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
                Log.e("EMAIL", "Error Processing Email", e)
                null
            }
        }
    }

    // Authenticate user with Google Sign-in
    suspend fun loginWithGoogle(activity: Activity, tokenManager: TokenManager): String? {
        val tokens = GoogleCredentialClient.requestIdToken(activity) ?: return null

        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(IDToken) {
                    idToken = tokens.idToken
                    provider = Google
                    nonce = tokens.rawNonce
                }

                val jwt = SupabaseClient.client.auth.currentAccessTokenOrNull()
                val refreshToken = SupabaseClient.client.auth.currentSessionOrNull()?.refreshToken

                if(jwt != null) {
                    tokenManager.saveJWT(jwt)
                    if(refreshToken != null) tokenManager.saveRefreshToken(refreshToken)
                }
                jwt
            } catch (e: Exception) {
                Log.e("SUPABASE", "Google Sign-in Error", e)
                null
            }
        }
    }

    // Browser-based OAuth. Returns as soon as the custom tab opens.
    // The session arrives via deep link and is picked up by sessionStatus collector in RoleCallApplication.kt
    suspend fun loginWithGithub() {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Github)
            } catch (e: Exception) {
                Log.e("SUPABASE", "Github Sign-in Error", e)
        }
        }
    }

    // Browser-based Microsoft OAuth, same shape as GitHub. Session arrives via deep link.
    suspend fun loginWithMicrosoft() {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Azure) {
                    scopes.addAll(listOf("email", "profile", "openid"))
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Microsoft Sign-in Error")
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
