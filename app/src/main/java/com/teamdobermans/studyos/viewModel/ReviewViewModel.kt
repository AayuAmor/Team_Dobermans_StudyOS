package com.teamdobermans.studyos.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.repo.ReviewReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReviewUiState {
    object Idle : ReviewUiState

    object Loading : ReviewUiState

    data class Loaded(
        val note: NoteModel,
        val reminderEnabled: Boolean,
        val reviewStage: Int,
        val nextReviewAt: Long,
        val lastReviewedAt: Long
    ) : ReviewUiState

    object Saving : ReviewUiState

    data class Error(val message: String, val previousState: Loaded? = null) : ReviewUiState
}

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReviewReminderRepository(application)

    private val _state = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val reviewState: StateFlow<ReviewUiState> = _state.asStateFlow()

    private var currentNote: NoteModel? = null

    fun loadNote(noteId: String) {
        if (noteId.isBlank()) { _state.value = ReviewUiState.Idle; return }
        _state.value = ReviewUiState.Loading
        viewModelScope.launch {
            repo.observeNote(noteId).collect { note ->
                if (note == null) {
                    _state.value = ReviewUiState.Idle
                    return@collect
                }
                currentNote = note
                _state.value = ReviewUiState.Loaded(
                    note            = note,
                    reminderEnabled = note.reminderEnabled,
                    reviewStage     = note.reviewStage,
                    nextReviewAt    = note.nextReviewAt,
                    lastReviewedAt  = note.lastReviewedAt
                )
            }
        }
    }

    fun toggleReminder() {
        val loaded = _state.value as? ReviewUiState.Loaded ?: return
        if (loaded.reminderEnabled) disableReminder() else enableReminder()
    }

    fun enableReminder() {
        val note = currentNote ?: return
        val previous = _state.value as? ReviewUiState.Loaded
        _state.value = ReviewUiState.Saving
        viewModelScope.launch {
            val success = repo.enableReminder(note)
            if (!success) {
                _state.value = ReviewUiState.Error("Failed to enable reminder.", previous)
            }
        }
    }

    fun disableReminder() {
        val noteId  = currentNote?.id ?: return
        val previous = _state.value as? ReviewUiState.Loaded
        _state.value = ReviewUiState.Saving
        viewModelScope.launch {
            val success = repo.disableReminder(noteId)
            if (!success) {
                _state.value = ReviewUiState.Error("Failed to disable reminder.", previous)
            }
        }
    }

    fun markReviewed() {
        val note = currentNote ?: return
        if (note.reviewStage >= ReviewReminderRepository.MAX_STAGE) return
        val previous = _state.value as? ReviewUiState.Loaded
        _state.value = ReviewUiState.Saving
        viewModelScope.launch {
            val success = repo.markReviewed(note)
            if (!success) {
                _state.value = ReviewUiState.Error("Failed to mark as reviewed.", previous)
            }
        }
    }

    fun dismissError() {
        val err = _state.value as? ReviewUiState.Error ?: return
        _state.value = err.previousState ?: ReviewUiState.Idle
    }
}