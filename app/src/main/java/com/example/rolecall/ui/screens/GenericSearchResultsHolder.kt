package com.example.rolecall.ui.screens

import com.example.rolecall.data.model.JobItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GenericSearchResultsHolder {
    private val _results = MutableStateFlow<List<JobItem>>(emptyList())
    val results: StateFlow<List<JobItem>> = _results.asStateFlow()

    fun setResults(jobs: List<JobItem>) {
        _results.value = jobs
    }

    fun clear() {
        _results.value = emptyList()
    }
}