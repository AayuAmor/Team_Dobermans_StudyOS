package com.teamdobermans.studyos.viewModel

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.PomodoroTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PomodoroViewModel : ViewModel() {

    private val _selectedTab   = MutableStateFlow(PomodoroTab.FOCUS)
    val selectedTab: StateFlow<PomodoroTab> = _selectedTab.asStateFlow()

    private val _focusMinutes  = MutableStateFlow(25f)
    val focusMinutes: StateFlow<Float> = _focusMinutes.asStateFlow()

    private val _shortMinutes  = MutableStateFlow(5f)
    val shortMinutes: StateFlow<Float> = _shortMinutes.asStateFlow()

    private val _longMinutes   = MutableStateFlow(15f)
    val longMinutes: StateFlow<Float> = _longMinutes.asStateFlow()

    private val _isRunning     = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _sessionsToday = MutableStateFlow(0)
    val sessionsToday: StateFlow<Int> = _sessionsToday.asStateFlow()

    private val _timeRemaining = MutableStateFlow(25 * 60L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    fun selectTab(tab: PomodoroTab) {
        if (_isRunning.value) return
        _selectedTab.value = tab
        resetTimer()
    }

    fun setFocusMinutes(m: Float) {
        _focusMinutes.value = m
        if (_selectedTab.value == PomodoroTab.FOCUS) resetTimer()
    }

    fun setShortMinutes(m: Float) {
        _shortMinutes.value = m
        if (_selectedTab.value == PomodoroTab.SHORT_BREAK) resetTimer()
    }

    fun setLongMinutes(m: Float) {
        _longMinutes.value = m
        if (_selectedTab.value == PomodoroTab.LONG_BREAK) resetTimer()
    }

    fun toggleTimer() { if (_isRunning.value) pauseTimer() else startTimer() }

    fun resetTimer() {
        pauseTimer()
        _timeRemaining.value = totalSecondsForTab()
    }

    private fun startTimer() {
        _isRunning.value = true
        countDownTimer = object : CountDownTimer(_timeRemaining.value * 1000L, 1000L) {
            override fun onTick(ms: Long) { _timeRemaining.value = ms / 1000L }
            override fun onFinish() {
                _isRunning.value = false
                _sessionsToday.value += 1
                _timeRemaining.value = totalSecondsForTab()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    private fun totalSecondsForTab(): Long = when (_selectedTab.value) {
        PomodoroTab.FOCUS       -> (_focusMinutes.value * 60).toLong()
        PomodoroTab.SHORT_BREAK -> (_shortMinutes.value * 60).toLong()
        PomodoroTab.LONG_BREAK  -> (_longMinutes.value * 60).toLong()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}

