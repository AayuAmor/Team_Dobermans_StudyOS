package com.teamdobermans.studyos.viewModel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.data.analytics.AnalyticsRepository
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.ReviewReminderRepository
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val userName: String = "Learner",
    val todayTasks: List<Task> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val auth           = FirebaseAuth.getInstance()
    private val taskRepository = TaskRepository()
    private val analytics      = AnalyticsRepository()
    private val reviewRepo     = ReviewReminderRepository(application)

    private val _userName = MutableStateFlow(resolveUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _state = MutableStateFlow(
        DashboardUiState(todayTasks = taskRepository.getTodaysTasks())
    )
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    val streakCount: StateFlow<Int> = analytics.getCurrentStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val weeklyStudyHours: StateFlow<Float> = analytics.getWeeklyStudyHours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val pendingTaskCount: StateFlow<Int> = analytics.getPendingTaskCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val dailyProgress: StateFlow<Float> = analytics.getDailyTaskProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val upcomingReviews: StateFlow<List<NoteModel>> = reviewRepo.getUpcomingReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timeLeft = MutableStateFlow(25 * 60L)
    val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    var onSessionComplete: (() -> Unit)? = null

    fun loadUserName() {
        _userName.value = resolveUserName()
    }

    private fun resolveUserName(): String {
        val user = auth.currentUser
        return user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "Learner"
    }

    fun refreshDashboard() {
        _state.value = _state.value.copy(todayTasks = taskRepository.getTodaysTasks())
    }

    fun toggleTimer() {
        if (_timerRunning.value) pauseTimer() else startTimer()
    }

    fun startFocusSession() {
        if (!_timerRunning.value) startTimer()
    }

    fun pauseFocusSession() {
        pauseTimer()
    }

    fun resetFocusSession() {
        countDownTimer?.cancel()
        _timerRunning.value = false
        _timeLeft.value     = 25 * 60L
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        _timerRunning.value = true

        countDownTimer = object : CountDownTimer(_timeLeft.value * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                _timerRunning.value = false
                _timeLeft.value     = 25 * 60L
                onSessionComplete?.invoke()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        _timerRunning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}