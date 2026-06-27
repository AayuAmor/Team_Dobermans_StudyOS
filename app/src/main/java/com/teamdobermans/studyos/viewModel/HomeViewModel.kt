package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.VisionGoalModel
import com.teamdobermans.studyos.repo.VisionBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val studyStreak: Int = 0,
    val weeklyHours: Float = 0f,
    val tasksCompleted: Int = 0,
    val totalTasks: Int = 0,
    val dailyGoalPercent: Float = 0f,
    val lastSubject: String = "",
    val lastNote: String = "",
    val flashcardsDueToday: Int = 0,
    val todayStudyMinutes: Int = 0,
    val goals: List<VisionGoalModel> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val visionBoardRepository = VisionBoardRepository()

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        listenToVisionGoals()
    }

    private fun listenToVisionGoals() {
        viewModelScope.launch {
            visionBoardRepository.observePinnedGoals().collect { goals ->
                _state.value = _state.value.copy(goals = goals)
            }
        }
    }
}
