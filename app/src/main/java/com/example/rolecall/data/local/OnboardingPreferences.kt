package com.example.rolecall.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "onboarding_prefs")

object OnboardingPreferences {
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    fun isOnboardingComplete(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun setOnboardingComplete(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
        }
    }
}
