package com.teamdobermans.studyos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {

    private val _progress = MutableStateFlow(65f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timeLeft = MutableStateFlow(25 * 60L)
    val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()
}