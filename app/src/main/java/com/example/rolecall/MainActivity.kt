package com.example.rolecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.rolecall.data.local.OnboardingPreferences
import com.example.rolecall.navigation.RoleCallNavGraph
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.theme.RoleCallTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboardingComplete = runBlocking {
            OnboardingPreferences.isOnboardingComplete(this@MainActivity).first()
        }
        val startRoute = if (!onboardingComplete) Routes.ONBOARDING else null

        setContent {
            RoleCallTheme {
                val navController = rememberNavController()
                RoleCallNavGraph(
                    navController = navController,
                    startRouteOverride = startRoute
                )
            }
        }
    }
}