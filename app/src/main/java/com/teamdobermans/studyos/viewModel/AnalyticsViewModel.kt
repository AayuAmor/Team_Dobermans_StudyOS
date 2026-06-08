package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeeklyBarData(val day: String, val hours: Float)

data class SubjectBreakdownItem(
    val name: String,
    val percent: Float,
    val colorArgb: Long
)

data class AnalyticsUiState(
    val studyStreak: Int               = 14,
    val monthlyHours: Float            = 36.5f,
    val quizAccuracy: Float            = 0.78f,
    val focusScore: Int                = 72,
    val weeklyData: List<WeeklyBarData> = listOf(
        WeeklyBarData("Mon", 1.5f),
        WeeklyBarData("Tue", 2.0f),
        WeeklyBarData("Wed", 0.5f),
        WeeklyBarData("Thu", 3.0f),
        WeeklyBarData("Fri", 2.5f),
        WeeklyBarData("Sat", 1.0f),
        WeeklyBarData("Sun", 0.0f)
    ),
    val subjectBreakdown: List<SubjectBreakdownItem> = listOf(
        SubjectBreakdownItem("Mathematics",     0.35f, 0xFF5B4FD4),
        SubjectBreakdownItem("Physics",         0.25f, 0xFFE04F7B),
        SubjectBreakdownItem("Chemistry",       0.20f, 0xFF3A82E0),
        SubjectBreakdownItem("Computer Sci",    0.20f, 0xFF1D9E75)
    )
)

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()
}

