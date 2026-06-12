package com.teamdobermans.studyos.viewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.UserProfile
import com.teamdobermans.studyos.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle    : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String = "Profile updated successfully") : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _email = MutableStateFlow(repository.currentUserEmail())
    val email: StateFlow<String> = _email.asStateFlow()

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() = viewModelScope.launch {
        repository.getProfile()
            .onSuccess { loaded ->
                _profile.value = loaded
                _email.value   = loaded.email
            }
            .onFailure { }
    }

    fun validateProfile(name: String, email: String): Boolean {
        var valid = true

        _nameError.value = when {
            name.trim().isEmpty()  -> "Name cannot be empty".also { valid = false }
            name.trim().length < 2 -> "Name must be at least 2 characters".also { valid = false }
            else                   -> null
        }

        _emailError.value = when {
            email.trim().isEmpty()                                    -> "Email cannot be empty".also { valid = false }
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()   -> "Invalid email format".also { valid = false }
            else                                                      -> null
        }

        return valid
    }

    fun updateProfile(name: String, email: String) = viewModelScope.launch {
        if (!validateProfile(name, email)) return@launch

        _uiState.value = ProfileUiState.Loading
        repository.updateProfile(name.trim(), email.trim())
            .onSuccess {
                _profile.value  = _profile.value.copy(name = name.trim(), email = email.trim())
                _email.value    = email.trim()
                _saveResult.value = "Profile updated successfully"
                _uiState.value  = ProfileUiState.Success()
            }
            .onFailure {
                _uiState.value  = ProfileUiState.Error(it.message ?: "Update failed")
                _saveResult.value = it.message
            }
    }

    fun updateDisplayName(name: String) = viewModelScope.launch {
        repository.updateDisplayName(name)
            .onSuccess {
                _profile.value    = _profile.value.copy(name = name)
                _saveResult.value = "Profile updated"
            }
            .onFailure { _saveResult.value = it.message }
    }

    fun updatePassword(newPassword: String) = viewModelScope.launch {
        repository.updatePassword(newPassword)
            .onSuccess { _saveResult.value = "Password updated" }
            .onFailure { _saveResult.value = it.message }
    }

    fun clearResult() {
        _saveResult.value = null
        _uiState.value    = ProfileUiState.Idle
        _nameError.value  = null
        _emailError.value = null
    }
}
