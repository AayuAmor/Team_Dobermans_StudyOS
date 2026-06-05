package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val studyStreak: Int        = 15,
    val weeklyHours: Float      = 8.5f,
    val tasksCompleted: Int     = 3,
    val totalTasks: Int         = 7,
    val dailyGoalPercent: Float = 0.60f,
    val lastSubject: String     = "Software Development",
    val lastNote: String        = "Design Patterns",
    val flashcardsDueToday: Int = 12,
    val todayStudyMinutes: Int  = 95,
    val goals: List<String>     = listOf(
        "Complete DSA module",
        "Review Physics notes",
        "Score 90%+ on next quiz"
    )
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
}

