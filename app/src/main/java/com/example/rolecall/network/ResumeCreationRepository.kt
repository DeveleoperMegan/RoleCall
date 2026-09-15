package com.example.rolecall.network

import android.util.Log
import com.example.rolecall.data.remote.*
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.io.File

// Response model for GET /api/v1/templates
data class TemplatesResponse(
    val templates: List<TemplateInfo>
)

data class TemplateInfo(
    val name: String,
    val description: String
)

class ResumeCreationRepository(private val tokenManager: TokenManager) {

    private val client = HttpClient(Android) {
        followRedirects = true

        // ── Timeouts to survive Railway cold starts ─────────────────────────
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000   // 2 minutes total
            connectTimeoutMillis = 90_000    // 90 seconds to connect
            socketTimeoutMillis = 120_000    // 2 minutes waiting for data
        }

        // ── Auth plugin (same pattern as FastAPIRepository) ─────────────────
        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenManager.getJWT()
                    if (token != null) {
                        BearerTokens(
                            accessToken = token,
                            refreshToken = tokenManager.getRefreshToken() ?: ""
                        )
                    } else {
                        null
                    }
                }
                refreshTokens {
                    val newToken = AuthRepository.refreshAccessToken(tokenManager)
                    if (newToken != null) {
                        BearerTokens(
                            accessToken = newToken,
                            refreshToken = tokenManager.getRefreshToken() ?: ""
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    private val baseUrl = "https://resume-creation-backend-production.up.railway.app"
    private val gson = Gson()

    /**
     * Sends the structured resume data to the backend and returns the generation response.
     */
    suspend fun generateResume(request: ResumeGenerationRequest): Result<ResumeGenerationResponse> {
        return try {
            val json = gson.toJson(request)
            val response: HttpResponse = client.post("$baseUrl/api/v1/resumes/generate") {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = gson.fromJson(body, ResumeGenerationResponse::class.java)
                Result.success(data)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("RESUME_GEN", "Error body: $errorBody")
                Result.failure(Exception("Generate resume failed (${response.status}): $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("RESUME_GEN", "Error", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches the available template names from the backend.
     * Returns a list like ["classic", "modern", "compact"].
     */
    suspend fun getTemplates(): Result<List<String>> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/templates")
            if (response.status.value in 200..299) {
                val body: TemplatesResponse = response.body()
                Result.success(body.templates.map { it.name })
            } else {
                Log.e("RESUME_TEMPLATES", "Error: ${response.status}")
                Result.failure(Exception("Get templates failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("RESUME_TEMPLATES", "Error", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches the available templates with their descriptions.
     * Useful for a picker that displays descriptions alongside the names.
     */
    suspend fun getTemplatesWithDescriptions(): Result<List<TemplateInfo>> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/templates")
            if (response.status.value in 200..299) {
                val body: TemplatesResponse = response.body()
                Result.success(body.templates)
            } else {
                Log.e("RESUME_TEMPLATES", "Error: ${response.status}")
                Result.failure(Exception("Get templates failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("RESUME_TEMPLATES", "Error", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads the generated PDF to the cache directory and returns the local file path.
     */
    suspend fun downloadResume(resumeId: String, cacheDir: File): Result<String> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/resumes/$resumeId/download")
            if (response.status.value in 200..299) {
                val bytes: ByteArray = response.body()
                val file = File(cacheDir, "resume_$resumeId.pdf")
                file.writeBytes(bytes)
                Result.success(file.absolutePath)
            } else {
                Result.failure(Exception("Download resume failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("RESUME_DOWNLOAD", "Error", e)
            Result.failure(e)
        }
    }

    /**
     * Pre-warm the Railway service so the first real request is fast.
     */
    suspend fun warmup() {
        try {
            client.get("$baseUrl/health")
            Log.i("RESUME_WARMUP", "Service warmed up")
        } catch (e: Exception) {
            Log.w("RESUME_WARMUP", "Warmup failed: ${e.message}")
        }
    }
}