package com.example.rolecall.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.rolecall.data.mapper.JobMapper
import com.example.rolecall.navigation.Routes
import com.example.rolecall.network.FastAPIRepository
import com.example.rolecall.ui.components.WebSearchAnimation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebSearchAnimationViewModel @Inject constructor(
    private val repository: FastAPIRepository
) : ViewModel() {

    fun performSearch(query: String) {
        viewModelScope.launch {
            repository.browseJobs()
                .onSuccess { page ->
                    val items = page.items ?: emptyList()
                    val filtered = items.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.companyName?.contains(query, ignoreCase = true) == true ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    val jobs = filtered.map { item ->
                        com.example.rolecall.data.model.JobItem(
                            id = item.id,
                            title = item.title,
                            company = item.companyName ?: "Unknown",
                            location = "",
                            description = item.description,
                            matchScore = 0f,
                            maxSalary = item.maxSalary,
                            minSalary = item.minSalary,
                            postDate = item.postDate,
                            postUrl = item.postUrl
                        )
                    }
                    GenericSearchResultsHolder.setResults(jobs)
                }
        }
    }
}

@Composable
fun WebSearchAnimationScreen(
    navController: NavController,
    query: String
) {
    val viewModel: WebSearchAnimationViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.performSearch(query)
    }

    WebSearchAnimation(
        onFinished = {
            navController.navigate("search_results") {
                popUpTo(Routes.HOME) { inclusive = false }
            }
        }
    )
}
