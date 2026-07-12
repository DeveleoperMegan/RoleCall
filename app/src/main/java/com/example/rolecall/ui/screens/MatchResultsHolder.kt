package com.example.rolecall.ui.screens

import com.example.rolecall.data.model.JobItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple singleton that holds match results in memory.
 * UploadViewModel writes to it, ResultsScreen reads from it.
 * No Hilt injection needed – just a plain Kotlin object.
 */
object MatchResultsHolder {
    private val _results = MutableStateFlow<List<JobItem>>(emptyList())
    val results: StateFlow<List<JobItem>> = _results.asStateFlow()

    fun setResults(jobs: List<JobItem>) {
        _results.value = jobs
    }

    fun clear() {
        _results.value = emptyList()
    }
}