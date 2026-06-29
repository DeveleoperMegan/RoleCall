package com.example.rolecall.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rolecall.ui.screens.*

@Composable
fun RoleCallNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = if (uiState.isLoggedIn) Routes.UPLOAD else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        // Auth screens
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.SIGNUP) {
            SignupScreen(navController)
        }

        // Profile
        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }

        // Upload
        composable(Routes.UPLOAD) {
            UploadScreen(navController)
        }

        // Results (default)
        composable(Routes.RESULTS) {
            ResultsScreen(navController, matchHistoryId = null)
        }

        // Results (from history)
        composable(
            "results/{matchHistoryId}",
            arguments = listOf(navArgument("matchHistoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchHistoryId = backStackEntry.arguments?.getLong("matchHistoryId")
            ResultsScreen(navController, matchHistoryId)
        }

        // Job Detail
        composable(
            Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(navController, jobId)
        }

        // History
        composable(Routes.HISTORY) {
            HistoryScreen(navController)
        }
    }
}