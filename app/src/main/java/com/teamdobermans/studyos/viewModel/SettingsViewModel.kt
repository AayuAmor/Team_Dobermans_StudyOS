package com.teamdobermans.studyos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.repo.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(remindersEnabled = repository.getCachedReminderPreference())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Kept for backward compat with any existing callers
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _darkMode  = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        val enabled = repository.getReminderPreference()
        _uiState.value = _uiState.value.copy(
            remindersEnabled = enabled,
            loading = false
        )
    }

    fun setRemindersEnabled(enabled: Boolean) = viewModelScope.launch {
        // Optimistic update so the toggle feels instant
        _uiState.value = _uiState.value.copy(
            remindersEnabled = enabled,
            saving = true,
            errorMessage = null,
            successMessage = null
        )

        val success = repository.setReminderPreference(enabled)

        _uiState.value = _uiState.value.copy(
            saving = false,
            remindersEnabled = if (success) enabled else !enabled,
            successMessage = if (success) "Saved" else null,
            errorMessage   = if (!success) "Failed to save preference" else null
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage   = null
        )
    }

    fun toggleNotifications() { _notificationsEnabled.value = !_notificationsEnabled.value }
    fun toggleDarkMode()       { _darkMode.value = !_darkMode.value }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _signedOut.value = true
    }
}
