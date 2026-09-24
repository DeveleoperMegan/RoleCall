package com.example.rolecall

import android.app.Application
import com.example.rolecall.network.SupabaseClient
import com.example.rolecall.network.TokenManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────────────────────
// RoleCallApplication
//
// Runs once when the app process starts, before any Activity exists.
//
// Its job is to keep TokenManager synced with the Supabase session.
// There are two separate stores of the JWT:
//   1. supabase.auth  - Supabase's own session, refreshed automatically
//   2. TokenManager   - our encrypted copy, which FastAPIRepository reads
//                       when it attaches the Bearer header
//
// Nothing connects those two on its own.
//
// Collecting sessionStatus here covers every case of sign-in, because every sign-in and
// every refresh flows through it regardless of which provider was used.
// ──────────────────────────────────────────────────────────────────────────────


@HiltAndroidApp
class RoleCallApplication : Application() {

    // Hilt can't field inject this early, so pull TokenManager from the singleton graph.
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun tokenManager(): TokenManager
    }

    // Process scoped, not screen scoped because it must outlive any Activity.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val tokenManager = EntryPointAccessors
            .fromApplication(this, AppEntryPoint::class.java)
            .tokenManager()

        appScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        tokenManager.saveJWT(status.session.accessToken)
                        status.session.refreshToken.let { tokenManager.saveRefreshToken(it) }
                    }
                    is SessionStatus.NotAuthenticated -> tokenManager.clearTokens()
                    else -> Unit
                }
            }
        }
    }
}