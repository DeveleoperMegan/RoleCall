package com.example.rolecall.network

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID


data class GoogleTokens(val idToken: String, val rawNonce: String)


object GoogleCredentialClient {

    suspend fun requestIdToken(activity: Activity): GoogleTokens? {
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") {
                "%02x".format(it)
            }

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(SupabaseClient.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = CredentialManager.create(activity).getCredential(activity, request)
            val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
            GoogleTokens(idToken = credential.idToken, rawNonce = rawNonce)
        } catch (e: GetCredentialCancellationException) {
            Log.i("GOOGLE_OAUTH", "No Google account on the device")
            null
        } catch (e: Exception) {
            Log.e("GOOGLE_AUTH", "Credential request has failed")
            null
        }
    }

}