package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.VisionGoalModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VisionBoardViewModel : ViewModel() {

    private val _goalText        = MutableStateFlow("")
    val goalText: StateFlow<String> = _goalText.asStateFlow()

    private val _selectedEmoji   = MutableStateFlow("🏅")
    val selectedEmoji: StateFlow<String> = _selectedEmoji.asStateFlow()

    private val _targetValue     = MutableStateFlow("")
    val targetValue: StateFlow<String> = _targetValue.asStateFlow()

    private val _selectedSubject = MutableStateFlow("General")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _pinnedGoals     = MutableStateFlow<List<VisionGoalModel>>(emptyList())
    val pinnedGoals: StateFlow<List<VisionGoalModel>> = _pinnedGoals.asStateFlow()

    fun setGoalText(text: String)      { _goalText.value = text }
    fun setEmoji(emoji: String)        { _selectedEmoji.value = emoji }
    fun setTargetValue(value: String)  { _targetValue.value = value }
    fun selectSubject(subject: String) { _selectedSubject.value = subject }

    fun pinGoal() {
        if (_goalText.value.isBlank()) return
        _pinnedGoals.value = _pinnedGoals.value + VisionGoalModel(
            text        = _goalText.value,
            emoji       = _selectedEmoji.value,
            targetValue = _targetValue.value,
            subject     = _selectedSubject.value
        )
        _goalText.value    = ""
        _targetValue.value = ""
    }

    fun removeGoal(goal: VisionGoalModel) {
        _pinnedGoals.value = _pinnedGoals.value - goal
    }
}

