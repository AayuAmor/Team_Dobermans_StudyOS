package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.repo.NoteRepo
import com.teamdobermans.studyos.repo.NoteRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GENERAL_SUBJECT = "General"

data class SubjectUiModel(
    val id: String,
    val name: String,
    val notesCount: Int,
    val flashcardsCount: Int,
    val progressPercent: Float,
    val colorArgb: Long
)

data class StudyUiState(
    val searchQuery: String = "",
    val subjects: List<SubjectUiModel> = emptyList(),
    val recentNoteTitle: String? = null,
    val recentNoteFolder: String? = null,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

class StudyViewModel(
    private val noteRepo: NoteRepo = NoteRepoImpl()
) : ViewModel() {

    private val subjectColors = listOf(
        0xFF5B4FD4,
        0xFF493FAD,
        0xFF7B61FF,
        0xFF1D9E75,
        0xFF3A82E0,
        0xFFE07B39
    )

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            noteRepo.getNotes().collect { notes ->
                val groupedSubjects = notes
                    .groupBy { it.folder.trim().ifBlank { GENERAL_SUBJECT } }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                    .entries
                    .mapIndexed { index, entry ->
                        SubjectUiModel(
                            id = entry.key.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                                .ifBlank { "general" },
                            name = entry.key,
                            notesCount = entry.value.size,
                            flashcardsCount = 0,
                            progressPercent = 0f,
                            colorArgb = subjectColors[index % subjectColors.size]
                        )
                    }

                val recent = notes.maxByOrNull { it.lastActivityTime() }
                _state.value = _state.value.copy(
                    subjects = groupedSubjects,
                    recentNoteTitle = recent?.title?.ifBlank { "Untitled Note" },
                    recentNoteFolder = recent?.folder?.trim()?.ifBlank { GENERAL_SUBJECT },
                    isLoading = false,
                    isEmpty = notes.isEmpty()
                )
            }
        }
    }

    private fun NoteModel.lastActivityTime(): Long = when {
        updatedAt > 0L -> updatedAt
        timestamp > 0L -> timestamp
        createdAt > 0L -> createdAt
        else -> 0L
    }

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
