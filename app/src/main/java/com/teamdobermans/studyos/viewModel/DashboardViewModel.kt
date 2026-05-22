package com.teamdobermans.studyos

import android.os.CountDownTimer
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

    private var countDownTimer: CountDownTimer? = null

    var onSessionComplete: (() -> Unit)? = null

    fun toggleTimer() {
        if (_timerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
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
}