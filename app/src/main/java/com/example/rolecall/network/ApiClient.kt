package com.example.rolecall.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

class ApiClient(private val tokenManager: TokenManager) {

    val fastAPIClient = HttpClient(Android) {
        // Follow redirects if needed
        followRedirects = true

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
}