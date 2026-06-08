package com.teamdobermans.studyos.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SubjectUiModel(
    val id: String,
    val name: String,
    val notesCount: Int,
    val flashcardsCount: Int,
    val progressPercent: Float,
    val colorArgb: Long
)

data class StudyUiState(
    val searchQuery: String            = "",
    val subjects: List<SubjectUiModel> = emptyList(),
    val recentNoteTitle: String?       = null,
    val recentNoteFolder: String?      = null
)

class StudyViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        StudyUiState(
            subjects = listOf(
                SubjectUiModel("1", "Mathematics",     12, 24, 0.65f, 0xFF5B4FD4),
                SubjectUiModel("2", "Physics",          8, 18, 0.45f, 0xFFE04F7B),
                SubjectUiModel("3", "Chemistry",        6, 14, 0.30f, 0xFF3A82E0),
                SubjectUiModel("4", "Computer Science",15, 30, 0.80f, 0xFF1D9E75)
            ),
            recentNoteTitle  = "Design Patterns",
            recentNoteFolder = "Computer Science"
        )
    )
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    fun updateSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    val filteredSubjects: List<SubjectUiModel>
        get() {
            val q = _state.value.searchQuery.trim()
            return if (q.isEmpty()) _state.value.subjects
            else _state.value.subjects.filter { it.name.contains(q, ignoreCase = true) }
        }
}

