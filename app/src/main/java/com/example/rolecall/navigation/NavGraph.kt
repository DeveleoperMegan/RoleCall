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
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.ui.screens.*
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun RoleCallNavGraph(
    navController: NavHostController,
    startRouteOverride: String? = null
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val defaultStart = if (uiState.isLoggedIn) Routes.HOME else Routes.SIGNUP
    val startDestination = startRouteOverride ?: defaultStart

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(navController)
        }
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
            "job_detail/{jobJson}",
            arguments = listOf(navArgument("jobJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobJson = backStackEntry.arguments?.getString("jobJson") ?: ""
            val decodedJson = URLDecoder.decode(jobJson, StandardCharsets.UTF_8.toString())
            val job = try {
                Gson().fromJson(decodedJson, JobItem::class.java)
            } catch (e: Exception) {
                null
            }
            JobDetailScreen(navController, job)
        }
        composable(Routes.HISTORY) {
            HistoryScreen(navController)
        }
        composable(Routes.RESUME_LIST) {
            ResumeListScreen(navController)
        }
        composable(
            Routes.PREVIEW,
            arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            PreviewScreen(navController, resumeId)
        }
        composable(
            Routes.MATCHING_ANIMATION,
            arguments = listOf(navArgument("resumeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            MatchingAnimationScreen(navController, resumeId)
        }
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable(
            Routes.SEARCH_RESULTS,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsScreen(navController, query)
        }
    }
}