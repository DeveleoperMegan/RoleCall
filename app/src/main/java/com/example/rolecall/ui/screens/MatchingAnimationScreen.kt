package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.mapper.JobMapper
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.components.MatchingAnimation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingAnimationViewModel @Inject constructor(
    private val fastAPIRepository: FastAPIRepository
) : ViewModel() {

    fun startSearch(resumeId: String) {
        viewModelScope.launch {
            fastAPIRepository.searchJobs(resumeId)
                .onSuccess { json ->
                    val jobs = JobMapper.parseSearchResponseToJobs(json)
                    MatchResultsHolder.setResults(jobs)
                }
                .onFailure {
                    MatchResultsHolder.setResults(emptyList())
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

    // When the animation signals completion, wait 2 seconds then navigate
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
        onFinished = { animationComplete = true }
    )
}