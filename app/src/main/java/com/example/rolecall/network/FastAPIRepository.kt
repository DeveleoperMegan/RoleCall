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
            val response: HttpResponse = client.get("https://rolecall-backend-ccf565df.fastapicloud.dev/health")

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
}