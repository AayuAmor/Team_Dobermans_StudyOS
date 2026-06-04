package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.Model.NoteModel
import com.teamdobermans.studyos.Repo.NoteRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val repo = NoteRepoImpl()

    val notes: StateFlow<List<NoteModel>> = repo.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    fun createNote(title: String, body: String, folder: String) =
        viewModelScope.launch {
            try {
                val success = repo.createNote(title, body, folder)
                _saveResult.value = if (success) "SUCCESS" else "Failed to create note."
            } catch (e: Exception) {
                _saveResult.value = e.message ?: "Unknown error"
            }
        }

    fun updateNote(note: NoteModel) =
        viewModelScope.launch {
            try {
                val success = repo.updateNote(note)
                _saveResult.value = if (success) "SUCCESS" else "Failed to update note."
            } catch (e: Exception) {
                _saveResult.value = e.message ?: "Unknown error"
            }
        }

    fun deleteNote(noteId: String) =
        viewModelScope.launch { repo.deleteNote(noteId) }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}