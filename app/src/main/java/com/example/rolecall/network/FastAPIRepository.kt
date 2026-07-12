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
                Log.e("FAST_API", "Health check failed: ${response.status}")
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
                Log.i("FAST_API", "User Email: $responseBody")
                responseBody
            } else {
                Log.e("FAST_API", "Get me failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Get Me", e)
            null
        }
    }

    suspend fun uploadResume(file: File, mimeType: String): String? {
        return try {
            Log.i("FAST_API", "Uploading ${file.name} (${file.length()} bytes) as $mimeType")
            val response: HttpResponse = client.post("$baseUrl/api/v1/resumes/upload") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
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

    suspend fun searchJobs(resumeId: String): String? {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/search/$resumeId")
            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.i("FAST_API", "Search response: $responseBody")
                responseBody
            } else {
                Log.e("FAST_API", "Search failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Search", e)
            null
        }
    }
}