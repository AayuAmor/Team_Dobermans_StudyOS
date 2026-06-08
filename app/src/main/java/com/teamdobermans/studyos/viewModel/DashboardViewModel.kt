package com.teamdobermans.studyos.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val userName: String = "Learner",
    val todayTasks: List<Task> = emptyList(),
    val dailyStudyHours: Float = 1.6f,
    val weeklyStudyHours: Float = 8.5f,
    val streakCount: Int = 15,
    val quizAccuracy: Int = 78,
    val focusSessionsToday: Int = 3,
    val dailyProgress: Float = 65f
)

class DashboardViewModel : ViewModel() {

    private val taskRepository = TaskRepository()

    private val _state = MutableStateFlow(
        DashboardUiState(
            todayTasks = taskRepository.getTodaysTasks()
        )
    )
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private val _progress = MutableStateFlow(_state.value.dailyProgress)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timeLeft = MutableStateFlow(25 * 60L)
    val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    var onSessionComplete: (() -> Unit)? = null

    fun refreshDashboard() {
        val updatedTasks = taskRepository.getTodaysTasks()

        _state.value = _state.value.copy(
            todayTasks = updatedTasks
        )

        _progress.value = _state.value.dailyProgress
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startFocusSession() {
        if (!_timerRunning.value) {
            startTimer()
        }
    }

    fun pauseFocusSession() {
        pauseTimer()
    }

    fun resetFocusSession() {
        countDownTimer?.cancel()
        _timerRunning.value = false
        _timeLeft.value = 25 * 60L
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
                _timeLeft.value = 25 * 60L
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