package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.repo.FlashcardRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlashcardViewModel : ViewModel() {
    private val repo = FlashcardRepoImpl()

    val flashcards: StateFlow<List<FlashcardModel>> = repo.getFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    fun createFlashcard(question: String, answer: String, subject: String) =
        viewModelScope.launch {
            try {
                val ok = repo.createFlashcard(question, answer, subject)
                _saveResult.value = if (ok) "SUCCESS" else "Failed to save card."
            } catch (e: Exception) {
                _saveResult.value = e.message ?: "Unknown error"
            }
        }

    fun updateFlashcard(card: FlashcardModel) =
        viewModelScope.launch {
            try {
                val ok = repo.updateFlashcard(card)
                _saveResult.value = if (ok) "SUCCESS" else "Failed to update card."
            } catch (e: Exception) {
                _saveResult.value = e.message ?: "Unknown error"
            }
        }

    fun deleteFlashcard(cardId: String) =
        viewModelScope.launch { repo.deleteFlashcard(cardId) }

    fun clearSaveResult() { _saveResult.value = null }
}