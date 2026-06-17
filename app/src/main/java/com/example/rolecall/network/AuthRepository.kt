package com.example.rolecall.network

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AuthRepository {
    // Register user with email and password
    // Store JWT string on success
    suspend fun signUpWithEmail(emailInput: String, passwordInput: String, tokenManager: TokenManager): String? {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // Extract JWT
                val jwt = SupabaseClient.client.auth.currentAccessTokenOrNull()

                // Store JWT
                if (jwt != null) {
                    tokenManager.saveJWT(jwt)
                }

                jwt

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Authenticate user with email and password
    // Store JWT string on success
    suspend fun loginWithEmail(emailInput: String, passwordInput: String, tokenManager: TokenManager): String? {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // Extract JWT
                val jwt = SupabaseClient.client.auth.currentAccessTokenOrNull()

                // Store JWT
                if (jwt != null) {
                    tokenManager.saveJWT(jwt)
                }

                jwt

            } catch (e: Exception) {
                e.printStackTrace()
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
                tokenManager.clearJWT()
            }
        }
    }
}