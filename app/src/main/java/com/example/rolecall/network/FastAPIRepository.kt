package com.example.rolecall.network

import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText


class FastAPIRepository(tokenManager: TokenManager) {
    private val apiClient = ApiClient(tokenManager)
    private val client = apiClient.fastAPIClient

    suspend fun checkHealth(): String? {
        return try {
            val response: HttpResponse = client.get("http://10.0.2.2:8000/health")

            if(response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.i("FAST_API", responseBody)
                responseBody
            } else {
                Log.e("FAST_API", "Response: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Health Check", e)
            null
        }
    }

    suspend fun getUserEmail(): String? {
        return try {
            val response: HttpResponse = client.get("http://10.0.2.2:8000/me")

            if(response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.i("FAST_API", "User Email: ${response.bodyAsText()}")
                responseBody
            } else {
                Log.e("FAST_API", "Response: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Get Me", e)
            null
        }
    }
}