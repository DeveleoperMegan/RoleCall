package com.example.rolecall.network

import android.util.Log
import com.example.rolecall.data.remote.ProfilePatch
import com.example.rolecall.data.remote.ProfileRead
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
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

class ProfileRepository(private val tokenManager: TokenManager) {

    private val client = HttpClient(Android) {
        followRedirects = true
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 60_000
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenManager.getJWT()
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = tokenManager.getRefreshToken() ?: "")
                    } else null
                }
                refreshTokens {
                    val newToken = AuthRepository.refreshAccessToken(tokenManager)
                    if (newToken != null) {
                        BearerTokens(accessToken = newToken, refreshToken = tokenManager.getRefreshToken() ?: "")
                    } else null
                }
            }
        }
    }

    private val baseUrl = "https://rolecallbackend-production.up.railway.app"
    private val gson = Gson()

    /** GET /api/v1/user/info */
    suspend fun getUserInfo(): Result<ProfileRead> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v1/user/info")
            if (response.status.value in 200..299) {
                val data = gson.fromJson(response.bodyAsText(), ProfileRead::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Get user info failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "getUserInfo error", e)
            Result.failure(e)
        }
    }

    /** PATCH /api/v1/user/update */
    suspend fun updateUserInfo(patch: ProfilePatch): Result<ProfileRead> {
        return try {
            val response: HttpResponse = client.patch("$baseUrl/api/v1/user/update") {
                contentType(ContentType.Application.Json)
                setBody(gson.toJson(patch))
            }
            if (response.status.value in 200..299) {
                val data = gson.fromJson(response.bodyAsText(), ProfileRead::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Update user info failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "updateUserInfo error", e)
            Result.failure(e)
        }
    }

    /** POST /api/v1/user/avatar */
    suspend fun uploadAvatar(file: File, mimeType: String): Result<ProfileRead> {
        return try {
            val response: HttpResponse = client.post("$baseUrl/api/v1/user/avatar") {
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
                val data = gson.fromJson(response.bodyAsText(), ProfileRead::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Upload avatar failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "uploadAvatar error", e)
            Result.failure(e)
        }
    }

    /** DELETE /api/v1/user/avatar */
    suspend fun deleteAvatar(): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete("$baseUrl/api/v1/user/avatar")
            if (response.status.value in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete avatar failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "deleteAvatar error", e)
            Result.failure(e)
        }
    }
}
