package com.example.rolecall.network

import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

class FastAPIRepository(tokenManager: TokenManager) {
    private val apiClient = ApiClient(tokenManager)
    private val client = apiClient.fastAPIClient

    private val baseUrl = "https://rolecallbackend-production.up.railway.app"

    suspend fun checkHealth(): String? {
        return try {
            val response: HttpResponse = client.get("$baseUrl/health")

            if (response.status.value in 200..299) {
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
            val response: HttpResponse = client.get("$baseUrl/me")

            if (response.status.value in 200..299) {
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

    suspend fun uploadResume(file: File): String? {
        return try {
            val response: HttpResponse = client.post("$baseUrl/upload/resume") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "application/pdf")
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                            })
                        }
                    )
                )
            }

            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.i("FAST_API", "Upload response: $responseBody")
                responseBody
            } else {
                Log.e("FAST_API", "Upload failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Upload", e)
            null
        }
    }
}