package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.SubjectModel
import com.teamdobermans.studyos.repo.NoteRepo
import com.teamdobermans.studyos.repo.NoteRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectViewModel(
    private val noteRepo: NoteRepo = NoteRepoImpl()
) : ViewModel() {

    private val _subjects = MutableStateFlow<List<SubjectModel>>(emptyList())
    val subjects: StateFlow<List<SubjectModel>> = _subjects.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepo.getNotes().collect { notes ->
                _subjects.value = notes
                    .map { it.folder.trim().ifBlank { "General" } }
                    .distinctBy { it.lowercase() }
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)
                    .map { name -> SubjectModel(id = name.toSubjectId(), name = name) }
            }
        }
    }

    fun addSubject(name: String) {
        val clean = name.trim()
        if (clean.isBlank() || _subjects.value.any { it.name.equals(clean, ignoreCase = true) }) return
        _subjects.value = (_subjects.value + SubjectModel(clean.toSubjectId(), clean))
            .sortedBy { it.name.lowercase() }
    }

    private fun String.toSubjectId(): String =
        lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "general" }
}
