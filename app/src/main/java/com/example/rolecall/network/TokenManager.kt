package com.example.rolecall.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveJWT(token: String) {
        sharedPreferences.edit { putString("JWT_TOKEN", token) }
    }

    fun getJWT(): String? {
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit { putString("REFRESH_TOKEN", refreshToken) }
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("REFRESH_TOKEN", null)
    }

    fun clearTokens() {
        sharedPreferences.edit {
            remove("JWT_TOKEN")
            remove("REFRESH_TOKEN")
        }
    }


    fun clearJWT() {
        clearTokens()
    }
}