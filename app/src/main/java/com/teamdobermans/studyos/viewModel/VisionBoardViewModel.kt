package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.model.VisionGoalModel
import com.teamdobermans.studyos.repo.NoteRepoImpl
import com.teamdobermans.studyos.repo.VisionBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class VisionBoardViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val noteRepo = NoteRepoImpl()
    private val visionRepo = VisionBoardRepository()

    private val _goalText = MutableStateFlow("")
    val goalText: StateFlow<String> = _goalText.asStateFlow()

    private val _selectedEmoji = MutableStateFlow("🏅")
    val selectedEmoji: StateFlow<String> = _selectedEmoji.asStateFlow()

    private val _targetValue = MutableStateFlow("")
    val targetValue: StateFlow<String> = _targetValue.asStateFlow()

    private val _selectedSubject = MutableStateFlow("General")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val _pinnedGoals = MutableStateFlow<List<VisionGoalModel>>(emptyList())
    val pinnedGoals: StateFlow<List<VisionGoalModel>> = _pinnedGoals.asStateFlow()

    private val _editingGoal = MutableStateFlow<VisionGoalModel?>(null)
    val editingGoal: StateFlow<VisionGoalModel?> = _editingGoal.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun setGoalText(text: String) {
        _goalText.value = text
    }

    fun setEmoji(emoji: String) {
        _selectedEmoji.value = emoji
    }

    fun setTargetValue(value: String) {
        _targetValue.value = value
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun addSubject(subject: String) {
        val clean = subject.trim()
        if (clean.isNotBlank() && !_subjects.value.any { it.equals(clean, ignoreCase = true) }) {
            _subjects.value = (_subjects.value + clean).sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
    }

    fun startEditing(goal: VisionGoalModel) {
        _editingGoal.value = goal
    }

    fun stopEditing() {
        _editingGoal.value = null
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            _isAuthenticated.value = uid != null
            observeGoals()
        }
        observeGoals()
        observeNoteSubjects()
    }

    private fun observeNoteSubjects() {
        viewModelScope.launch {
            noteRepo.getNotes().collect { notes ->
                val dynamicSubjects = notes
                    .map { it.folder.trim().ifBlank { "General" } }
                    .distinctBy { it.lowercase() }
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)
                _subjects.value = dynamicSubjects
                _selectedSubject.value = when {
                    dynamicSubjects.isEmpty() -> "General"
                    _selectedSubject.value in dynamicSubjects -> _selectedSubject.value
                    else -> dynamicSubjects.first()
                }
            }
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            visionRepo.observePinnedGoals().collect { goals ->
                _pinnedGoals.value = goals
            }
        }
    }

    fun pinGoal(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val currentText = _goalText.value.trim()
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            onComplete(false, "Sign in to sync vision board goals")
            return
        }
        if (currentText.isBlank()) {
            onComplete(false, "Goal text is empty")
            return
        }

        viewModelScope.launch {
            visionRepo.addGoal(
                text = currentText,
                emoji = _selectedEmoji.value,
                targetValue = _targetValue.value.trim(),
                subject = _selectedSubject.value
            ).onSuccess {
                _goalText.value = ""
                _targetValue.value = ""
                onComplete(true, "Pinned successfully")
            }.onFailure {
                onComplete(false, "Failed to pin: ${it.message}")
            }
        }
    }

    fun editGoal(goal: VisionGoalModel) {
        viewModelScope.launch {
            visionRepo.updateGoal(goal).onSuccess { _editingGoal.value = null }
        }
    }

    fun removeGoal(goal: VisionGoalModel) {
        viewModelScope.launch {
            visionRepo.deleteGoal(goal.id)
        }
    }


}
