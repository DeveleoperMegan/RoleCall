package com.example.rolecall.network

import android.util.Log
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.data.remote.ResumeDetail
import com.google.gson.Gson
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
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.http.ContentType

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
            val token = apiClient.tokenManager.getJWT()
            Log.i("FAST_API", "Token being sent: ${token?.take(20)}...")
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

    // Fetch all previously uploaded resumes
    suspend fun getResumes(): List<ResumeItem>? {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/resumes/all")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Log.i("FAST_API", "Resumes: $body")
                Gson().fromJson(body, Array<ResumeItem>::class.java)?.toList()
            } else {
                Log.e("FAST_API", "Get resumes failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Get Resumes", e)
            null
        }
    }

    //delete previously uploaded resumes
    suspend fun deleteResume(resumeId: String): Boolean {
        return try {
            val response: HttpResponse = client.delete("$baseUrl/api/v1/resumes/$resumeId")
            if (response.status.value in 200..299) {
                Log.i("FAST_API", "Resume deleted: $resumeId")
                true
            } else {
                Log.e("FAST_API", "Delete resume failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Delete Resume", e)
            false
        }
    }

    //get resume details for preview
    suspend fun getResumeDetail(resumeId: String): ResumeDetail? {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/resumes/$resumeId")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Gson().fromJson(body, ResumeDetail::class.java)
            } else {
                Log.e("FAST_API", "Get resume detail failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Get Resume Detail", e)
            null
        }
    }

    // Rename endpoint – expects the backend to accept PATCH with a JSON body
    suspend fun renameResume(resumeId: String, newFilename: String): Boolean {
        return try {
            val response: HttpResponse = client.patch("$baseUrl/api/v1/resumes/$resumeId") {
                header("Content-Type", "application/json")
                setBody("""{"filename":"$newFilename"}""")
            }
            if (response.status.value in 200..299) {
                Log.i("FAST_API", "Resume renamed: $resumeId -> $newFilename")
                true
            } else {
                Log.e("FAST_API", "Rename failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Network Error during Rename", e)
            false
        }
    }
}