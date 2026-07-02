package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.data.analytics.AnalyticsRepository
import com.teamdobermans.studyos.model.WeeklyTrendPoint
import com.teamdobermans.studyos.model.WeeklyTrendType
import com.teamdobermans.studyos.repo.WeeklyAnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// Kept for backward-compatibility with AnalyticsCharts.kt and ProgressActivity
data class WeeklyBarData(val day: String, val hours: Float)

data class SubjectBreakdownItem(
    val name: String,
    val percent: Float,
    val colorArgb: Long
)

data class AnalyticsUiState(
    // Weekly trend chart fields
    val isLoading: Boolean = true,
    val selectedTrendType: WeeklyTrendType = WeeklyTrendType.STUDY_TIME,
    val weeklyTrendPoints: List<WeeklyTrendPoint> = emptyList(),
    val totalThisWeek: Float = 0f,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    // Legacy fields for ProgressActivity — populated from real data, no hardcoded values
    val studyStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalStudyDays: Int = 0,
    val lastStudyDate: String? = null,
    val monthlyHours: Float = 0f,
    val quizAccuracy: Float = 0f,
    val focusScore: Int = 0,
    val weeklyData: List<WeeklyBarData> = emptyList(),
    val subjectBreakdown: List<SubjectBreakdownItem> = emptyList()
)

class AnalyticsViewModel : ViewModel() {

    private val repo = WeeklyAnalyticsRepository()
    private val analyticsRepo = AnalyticsRepository()

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        reload()
        observeStreak()
    }

    fun selectTrendType(type: WeeklyTrendType) {
        _state.value = _state.value.copy(selectedTrendType = type)
        reload()
    }

    private fun observeStreak() {
        viewModelScope.launch {
            analyticsRepo.observeStreak().collect { streak ->
                _state.value = _state.value.copy(
                    studyStreak = streak.currentStreak,
                    longestStreak = streak.longestStreak,
                    totalStudyDays = streak.totalStudyDays,
                    lastStudyDate = streak.lastStudyDate
                )
            }
        }
    }

    private fun reload() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val type = _state.value.selectedTrendType
                val points = when (type) {
                    WeeklyTrendType.STUDY_TIME      -> repo.getWeeklyStudyTime()
                    WeeklyTrendType.TASKS_COMPLETED -> repo.getWeeklyTasksCompleted()
                    WeeklyTrendType.QUIZ_SCORE      -> repo.getWeeklyQuizScores()
                    WeeklyTrendType.REVIEW_COUNT    -> repo.getWeeklyReviewCount()
                }
                val total = computeTotal(type, points)
                val isEmpty = points.all { it.value == 0f }
                // Keep ProgressActivity's WeeklyBarChartCard fed with real study-time data
                val legacyBars = if (type == WeeklyTrendType.STUDY_TIME) {
                    points.map { WeeklyBarData(it.dayLabel, it.value / 60f) }
                } else {
                    _state.value.weeklyData
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    weeklyTrendPoints = points,
                    totalThisWeek = total,
                    isEmpty = isEmpty,
                    weeklyData = legacyBars,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun computeTotal(type: WeeklyTrendType, points: List<WeeklyTrendPoint>): Float {
        if (type == WeeklyTrendType.QUIZ_SCORE) {
            val nonZero = points.filter { it.value > 0f }
            return if (nonZero.isEmpty()) 0f
            else nonZero.sumOf { it.value.toDouble() }.toFloat() / nonZero.size
        }
        return points.sumOf { it.value.toDouble() }.toFloat()
    }
}
