package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.data.remote.JobPostingItem
import com.example.rolecall.network.FastAPIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchResultsUiState {
    data object Loading : SearchResultsUiState()
    data class Success(val jobs: List<JobItem>) : SearchResultsUiState()
    data class Error(val message: String) : SearchResultsUiState()
}

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val repository: FastAPIRepository
) : ViewModel() {

    var uiState by mutableStateOf<SearchResultsUiState>(SearchResultsUiState.Loading)
        private set

    fun search(query: String) {
        viewModelScope.launch {
            uiState = SearchResultsUiState.Loading
            repository.browseJobs()
                .onSuccess { page ->
                    val items = page.items ?: emptyList()
                    // Filter locally by title/company/description containing query
                    val filtered = items.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.companyName?.contains(query, ignoreCase = true) == true ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    val jobs = filtered.map { item -> item.toJobItem() }
                    uiState = SearchResultsUiState.Success(jobs)
                }
                .onFailure { error ->
                    uiState = SearchResultsUiState.Error(error.message ?: "Search failed")
                }
        }
    }

    private fun JobPostingItem.toJobItem(): JobItem {
        return JobItem(
            id = id,
            title = title,
            company = companyName ?: "Unknown",
            location = "",
            description = description,
            matchScore = 0f,  // generic browse has no match score
            maxSalary = maxSalary,
            minSalary = minSalary,
            postDate = postDate,
            postUrl = postUrl,
            matchingPhrases = emptyList(),
            keySkills = emptyList()
        )
    }
}