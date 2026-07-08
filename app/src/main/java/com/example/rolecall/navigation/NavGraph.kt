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

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.SIGNUP) {
            SignupScreen(navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }
        composable(Routes.UPLOAD) {
            UploadScreen(navController)
        }
        composable(Routes.RESULTS) {
            ResultsScreen(navController, matchHistoryId = null)
        }
        composable(
            "results/{matchHistoryId}",
            arguments = listOf(navArgument("matchHistoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchHistoryId = backStackEntry.arguments?.getLong("matchHistoryId")
            ResultsScreen(navController, matchHistoryId)
        }
        composable(
            Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(navController, jobId)
        }
        composable(Routes.HISTORY) {
            HistoryScreen(navController)
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController)
        }
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(navController)
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController)
        }
        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(navController)
        }
    }
}