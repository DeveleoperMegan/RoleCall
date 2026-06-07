package com.example.rolecall.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rolecall.ui.screens.*

@Composable
fun RoleCallNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.UPLOAD) {
        composable(Routes.UPLOAD) { UploadScreen(navController) }
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