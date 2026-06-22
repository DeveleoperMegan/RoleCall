package com.example.rolecall.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rolecall.network.TokenManager
import com.example.rolecall.ui.screens.*


@Composable
fun RoleCallNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val startRoute = if (tokenManager.getJWT() != null) {
        Routes.UPLOAD
    } else {
        Routes.LOGIN
    }

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { token ->
                    // TODO: Delete after testing
                    Log.d("SUPABASE", "Success. Supabase JWT Token: $token")

                    navController.navigate(Routes.UPLOAD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.UPLOAD) {
            UploadScreen(navController,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.UPLOAD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESULTS) { ResultsScreen(navController) }

        composable(
            Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(navController, jobId)
        }

        composable(Routes.HISTORY) { HistoryScreen(navController) }
    }
}