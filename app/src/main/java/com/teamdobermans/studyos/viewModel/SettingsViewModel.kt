package com.teamdobermans.studyos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.repo.ProfileRepository
import com.teamdobermans.studyos.repo.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val dailyStudyGoalMinutes: Int = 120,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val userEmail: String = "",
    val userName: String = "Student",
    val providerName: String = "Firebase Auth",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val repository = SettingsRepository(application)
    private val profileRepository = ProfileRepository()

    private val _state = MutableStateFlow(
        SettingsUiState(
            remindersEnabled = repository.getCachedReminderPreference(),
            reminderHour = repository.getCachedReminderTime().first,
            reminderMinute = repository.getCachedReminderTime().second,
            dailyStudyGoalMinutes = repository.getCachedDailyStudyGoalMinutes(),
            focusDurationMinutes = repository.getCachedFocusDurationMinutes(),
            breakDurationMinutes = repository.getCachedBreakDurationMinutes(),
            isAuthenticated = auth.currentUser != null,
            userEmail = auth.currentUser?.email.orEmpty(),
            userName = resolveUserName(),
            providerName = resolveProviderName()
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
            val reminderEnabled = repository.getReminderPreference()
            val reminderTime = repository.getReminderTime()
            val dailyGoal = repository.getDailyStudyGoalMinutes()
            val focusMinutes = repository.getFocusDurationMinutes()
            val breakMinutes = repository.getBreakDurationMinutes()
            _state.value = _state.value.copy(
                remindersEnabled = reminderEnabled,
                reminderHour = reminderTime.first,
                reminderMinute = reminderTime.second,
                dailyStudyGoalMinutes = dailyGoal,
                focusDurationMinutes = focusMinutes,
                breakDurationMinutes = breakMinutes,
                isLoading = false,
                isAuthenticated = auth.currentUser != null,
                userEmail = auth.currentUser?.email.orEmpty(),
                userName = resolveUserName(),
                providerName = resolveProviderName()
            )
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(remindersEnabled = enabled)
        viewModelScope.launch {
            repository.setReminderPreference(enabled)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        _state.value = _state.value.copy(reminderHour = h, reminderMinute = m)
        viewModelScope.launch { repository.setReminderTime(h, m) }
    }

    fun setDailyStudyGoalMinutes(minutes: Int) {
        val clean = minutes.coerceIn(15, 720)
        _state.value = _state.value.copy(dailyStudyGoalMinutes = clean)
        viewModelScope.launch { repository.setDailyStudyGoalMinutes(clean) }
    }

    fun setFocusDurationMinutes(minutes: Int) {
        val clean = minutes.coerceIn(5, 120)
        _state.value = _state.value.copy(focusDurationMinutes = clean)
        viewModelScope.launch { repository.setFocusDurationMinutes(clean) }
    }

    fun setBreakDurationMinutes(minutes: Int) {
        val clean = minutes.coerceIn(1, 60)
        _state.value = _state.value.copy(breakDurationMinutes = clean)
        viewModelScope.launch { repository.setBreakDurationMinutes(clean) }
    }

    fun updateDisplayName(name: String) {
        val clean = name.trim()
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            profileRepository.updateDisplayName(clean)
                .onSuccess {
                    _state.value = _state.value.copy(
                        userName = clean,
                        isLoading = false,
                        successMessage = "Profile updated"
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Failed to update profile"
                    )
                }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }

    fun signOut() {
        auth.signOut()
        _signedOut.value = true
    }

    private fun resolveUserName(): String {
        val user = auth.currentUser
        return user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Student"
    }

    private fun resolveProviderName(): String {
        val user = auth.currentUser ?: return "Not signed in"
        return user.providerData
            .firstOrNull { it.providerId != "firebase" }
            ?.providerId
            ?.replace(".com", "")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Email"
    }
}
