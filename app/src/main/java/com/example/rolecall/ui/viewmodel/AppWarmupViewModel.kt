package com.example.rolecall.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.network.ResumeCreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppWarmupViewModel @Inject constructor(
    private val resumeCreationRepository: ResumeCreationRepository
) : ViewModel() {
    fun warmup() {
        viewModelScope.launch {
            resumeCreationRepository.warmup()
        }
    }
}