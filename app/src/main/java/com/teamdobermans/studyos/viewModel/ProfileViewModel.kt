package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _email      = MutableStateFlow(repository.currentUserEmail())
    val email: StateFlow<String> = _email.asStateFlow()

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult.asStateFlow()

    fun updateDisplayName(name: String) = viewModelScope.launch {
        repository.updateDisplayName(name)
            .onSuccess { _saveResult.value = "Profile updated" }
            .onFailure { _saveResult.value = it.message }
    }

    fun updatePassword(newPassword: String) = viewModelScope.launch {
        repository.updatePassword(newPassword)
            .onSuccess { _saveResult.value = "Password updated" }
            .onFailure { _saveResult.value = it.message }
    }

    fun clearResult() { _saveResult.value = null }
}

