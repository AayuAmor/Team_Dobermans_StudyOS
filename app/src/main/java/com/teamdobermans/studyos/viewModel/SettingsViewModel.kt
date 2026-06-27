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
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val isLoading: Boolean = true,
    val userEmail: String = "",
    val userName: String = "Learner"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val repository = SettingsRepository(application)

    private val _state = MutableStateFlow(
        SettingsUiState(
            remindersEnabled = repository.getCachedReminderPreference(),
            reminderHour = repository.getCachedReminderTime().first,
            reminderMinute = repository.getCachedReminderTime().second,
            userEmail = auth.currentUser?.email.orEmpty(),
            userName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: auth.currentUser?.email?.substringBefore("@")
                ?: "Learner"
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val enabled = repository.getReminderPreference()
            val time = repository.getReminderTime()
            val user = auth.currentUser
            _state.value = _state.value.copy(
                remindersEnabled = enabled,
                reminderHour = time.first,
                reminderMinute = time.second,
                isLoading = false,
                userEmail = user?.email.orEmpty(),
                userName = user?.displayName?.takeIf { it.isNotBlank() }
                    ?: user?.email?.substringBefore("@")
                    ?: "Learner"
            )
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(remindersEnabled = enabled)
        viewModelScope.launch { repository.setReminderPreference(enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        _state.value = _state.value.copy(reminderHour = h, reminderMinute = m)
        viewModelScope.launch { repository.setReminderTime(h, m) }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _signedOut.value = true
    }
}
