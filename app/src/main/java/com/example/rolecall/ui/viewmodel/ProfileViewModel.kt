package com.example.rolecall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rolecall.data.remote.ProfilePatch
import com.example.rolecall.data.remote.ProfileRead
import com.example.rolecall.data.repository.JobRepository
import com.example.rolecall.network.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var profile by mutableStateOf<ProfileRead?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val savedJobsCount: StateFlow<Int> = jobRepository.getAllSavedJobs()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val appliedJobsCount: StateFlow<Int> = jobRepository.getAppliedJobs()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val matchHistoryCount: StateFlow<Int> = jobRepository.getAllMatchHistory()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            profileRepository.getUserInfo()
                .onSuccess { profile = it }
                .onFailure { errorMessage = it.message ?: "Failed to load profile" }
            isLoading = false
        }
    }

    fun updateProfile(firstName: String?, lastName: String?, title: String?) {
        viewModelScope.launch {
            isLoading = true
            profileRepository.updateUserInfo(ProfilePatch(firstName, lastName, title))
                .onSuccess { profile = it; errorMessage = null }
                .onFailure { errorMessage = it.message ?: "Failed to update profile" }
            isLoading = false
        }
    }

    fun uploadAvatar(file: File, mimeType: String) {
        viewModelScope.launch {
            isLoading = true
            profileRepository.uploadAvatar(file, mimeType)
                .onSuccess { profile = it; errorMessage = null }
                .onFailure { errorMessage = it.message ?: "Failed to upload avatar" }
            isLoading = false
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            isLoading = true
            profileRepository.deleteAvatar()
                .onSuccess { profile = profile?.copy(avatarUrl = null); errorMessage = null }
                .onFailure { errorMessage = it.message ?: "Failed to delete avatar" }
            isLoading = false
        }
    }

    fun clearError() { errorMessage = null }
}

