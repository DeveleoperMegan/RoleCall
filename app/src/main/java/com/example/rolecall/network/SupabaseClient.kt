package com.example.rolecall.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.ktor.client.engine.android.Android

object SupabaseClient {
    private const val SUPABASE_URL = "https://abakgkpdqqhmglgzires.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_SQPGs5vStiliD6hQ1GlJTQ_nhdAWpbU"
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