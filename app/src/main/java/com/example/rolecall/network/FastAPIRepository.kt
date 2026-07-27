package com.example.rolecall.network

import android.util.Log
import com.example.rolecall.data.remote.ResumeDetail
import com.example.rolecall.data.remote.ResumeItem
import com.example.rolecall.data.remote.UploadJsonResponse
import com.google.gson.Gson
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.io.File

/**
 * Single point of contact for all RoleCall backend API calls.
 * Uses the Ktor client from [ApiClient] which automatically attaches the user’s
 * JWT as a Bearer token. Every public function returns [Result] so that ViewModels
 * can handle success and failure uniformly.
 */
class FastAPIRepository(tokenManager: TokenManager) {

    private val apiClient = ApiClient(tokenManager)
    private val client = apiClient.fastAPIClient
    private val baseUrl = "https://rolecallbackend-production.up.railway.app"
    private val gson = Gson()

    // ── Health / Meta ────────────────────────────────────────────────────────

    /** GET /health */
    suspend fun checkHealth(): Result<String> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/health")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Log.i("FAST_API", "Health: $body")
                Result.success(body)
            } else {
                Result.failure(Exception("Health check failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Health Error", e)
            Result.failure(e)
        }
    }

    /** GET /me – returns the authenticated user’s email */
    suspend fun getUserEmail(): Result<String> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/me")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Log.i("FAST_API", "User Email: $body")
                Result.success(body)
            } else {
                Result.failure(Exception("Get me failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Get Me Error", e)
            Result.failure(e)
        }
    }

    // ── Resumes ──────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/resumes/upload
     * Uploads a résumé file (PDF, image, text) and returns the parsed response.
     */
    suspend fun uploadResume(file: File, mimeType: String): Result<UploadJsonResponse> {
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
                val body = response.bodyAsText()
                Log.i("FAST_API", "Upload response: $body")
                val data = gson.fromJson(body, UploadJsonResponse::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Upload Error", e)
            Result.failure(e)
        }
    }

    /**
     * GET /api/v1/resumes/all
     * Returns the list of resumes uploaded by the authenticated user.
     */
    suspend fun getResumes(): Result<List<ResumeItem>> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/resumes/all")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Log.i("FAST_API", "Resumes: $body")
                val list = gson.fromJson(body, Array<ResumeItem>::class.java)?.toList()
                Result.success(list ?: emptyList())
            } else {
                Result.failure(Exception("Get resumes failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Get Resumes Error", e)
            Result.failure(e)
        }
    }

    /**
     * GET /api/v1/resumes/{resumeId}
     * Returns full details for a single resume (including file URL and raw text).
     */
    suspend fun getResumeDetail(resumeId: String): Result<ResumeDetail> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/resumes/$resumeId")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val detail = gson.fromJson(body, ResumeDetail::class.java)
                Result.success(detail)
            } else {
                Result.failure(Exception("Get resume detail failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Get Resume Detail Error", e)
            Result.failure(e)
        }
    }

    /**
     * DELETE /api/v1/resumes/{resumeId}
     * Permanently deletes a resume and its embedding.
     */
    suspend fun deleteResume(resumeId: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete("$baseUrl/api/v1/resumes/$resumeId")
            if (response.status.value in 200..299) {
                Log.i("FAST_API", "Resume deleted: $resumeId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete resume failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Delete Resume Error", e)
            Result.failure(e)
        }
    }

    /**
     * PATCH /api/v1/resumes/{resumeId}
     * Renames an existing resume.
     */
    suspend fun renameResume(resumeId: String, newFilename: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.patch("$baseUrl/api/v1/resumes/$resumeId") {
                contentType(ContentType.Application.Json)
                setBody("""{"filename":"$newFilename"}""")
            }
            if (response.status.value in 200..299) {
                Log.i("FAST_API", "Resume renamed: $resumeId -> $newFilename")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Rename failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Rename Error", e)
            Result.failure(e)
        }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/search/{resumeId}
     * Performs semantic search using the given resume ID and returns the raw JSON.
     * (Callers can parse the JSON with Gson as before.)
     */
    suspend fun searchJobs(resumeId: String): Result<String> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/search/$resumeId")
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                Log.i("FAST_API", "Search response: $body")
                Result.success(body)
            } else {
                Result.failure(Exception("Search failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FAST_API", "Search Error", e)
            Result.failure(e)
        }
    }
}