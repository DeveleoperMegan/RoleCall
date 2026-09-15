package com.example.rolecall.network

import com.example.rolecall.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.ktor.client.engine.android.Android

object SupabaseClient {
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY
    const val GOOGLE_WEB_CLIENT_ID = "169840516711-mccb7u0n6ebli8knpvbp18rds7okbh4e.apps.googleusercontent.com"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        httpEngine = Android.create()

        install(Auth) {
            autoLoadFromStorage = true
            alwaysAutoRefresh = true
        }
    }
}