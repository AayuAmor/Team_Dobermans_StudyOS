package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.NoteRepoImpl
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AutoSaveStatus { IDLE, SAVING, SAVED, FAILED }

class NoteViewModel : ViewModel() {
    private val repo = NoteRepoImpl()
    private val taskRepo = TaskRepository

    val notes: StateFlow<List<NoteModel>> = repo.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    private val _linkedTasks = MutableStateFlow<List<Task>>(emptyList())
    val linkedTasks: StateFlow<List<Task>> = _linkedTasks

    private val _currentEditingNoteId = MutableStateFlow<String?>(null)
    val currentEditingNoteId: StateFlow<String?> = _currentEditingNoteId

    private val _autoSaveStatus = MutableStateFlow(AutoSaveStatus.IDLE)
    val autoSaveStatus: StateFlow<AutoSaveStatus> = _autoSaveStatus

    private var autoSaveJob: Job? = null

    fun loadLinkedTasks(noteId: String) {
        _linkedTasks.value = taskRepo.getTasksForNote(noteId)
    }

    fun clearLinkedTasks() {
        _linkedTasks.value = emptyList()
    }

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

    fun onEditorChanged(noteId: String?, title: String, body: String, folder: String) {
        if (noteId != null && _currentEditingNoteId.value == null) {
            _currentEditingNoteId.value = noteId
        }
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200)
            autoSaveDraft(title, body, folder)
        }
    }

    private suspend fun autoSaveDraft(title: String, body: String, folder: String) {
        if (title.isBlank() && body.isBlank()) return
        _autoSaveStatus.value = AutoSaveStatus.SAVING
        val effectiveTitle = title.ifBlank { "Untitled Note" }
        val noteId = _currentEditingNoteId.value
        val success = if (noteId != null) {
            repo.autoSaveNote(NoteModel(id = noteId, title = effectiveTitle, body = body, folder = folder))
        } else {
            val newId = repo.createNoteAndReturnId(effectiveTitle, body, folder)
            if (newId != null) {
                _currentEditingNoteId.value = newId
                true
            } else {
                false
            }
        }
        _autoSaveStatus.value = if (success) AutoSaveStatus.SAVED else AutoSaveStatus.FAILED
        if (success) {
            delay(3000)
            if (_autoSaveStatus.value == AutoSaveStatus.SAVED) {
                _autoSaveStatus.value = AutoSaveStatus.IDLE
            }
        }
    }

    fun clearEditingNote() {
        autoSaveJob?.cancel()
        autoSaveJob = null
        _currentEditingNoteId.value = null
        _autoSaveStatus.value = AutoSaveStatus.IDLE
    }

    fun clearAutoSaveStatus() {
        _autoSaveStatus.value = AutoSaveStatus.IDLE
    }
}
