package com.example.rolecall.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.remote.MatchJsonItem
import com.example.rolecall.data.remote.SearchJsonResponse
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.components.MatchingAnimation
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MatchingAnimationViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    private val gson = Gson()

    fun startSearch(resumeId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fastAPIRepository.searchJobs(resumeId)
                    .onSuccess { json ->
                        val response = try {
                            gson.fromJson(json, SearchJsonResponse::class.java)
                        } catch (e: Exception) { null }
                        val matchList = response?.matches ?: emptyList()
                        val jobs = matchList.map { match ->
                            JobItem(
                                id = match.id,
                                title = match.title,
                                company = match.companyName ?: "Unknown",
                                location = "",
                                description = match.description,
                                matchScore = (match.similarity * 100).toFloat(),
                                maxSalary = match.maxSalary,
                                minSalary = match.minSalary,
                                postDate = match.postDate,
                                postUrl = match.postUrl
                            )
                        }
                        MatchResultsHolder.setResults(jobs)
                    }
            }
        }
    }
}

@Composable
fun MatchingAnimationScreen(
    navController: NavController,
    resumeId: String
) {
    val viewModel: MatchingAnimationViewModel = hiltViewModel()
    var animationComplete by remember { mutableStateOf(false) }

    // Start the search immediately
    LaunchedEffect(Unit) {
        viewModel.startSearch(resumeId)
    }

    // When the animation signals completion, wait 2 seconds (to show the banner) then navigate
    LaunchedEffect(animationComplete) {
        if (animationComplete) {
            delay(2000L)
            navController.navigate(Routes.RESULTS) {
                popUpTo(Routes.UPLOAD) { inclusive = false }
            }
        }
    }

    MatchingAnimation(
        modifier = Modifier.fillMaxSize(),
        onComplete = { animationComplete = true }
    )
}