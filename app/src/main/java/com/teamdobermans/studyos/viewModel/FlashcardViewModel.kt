package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.repo.FlashcardRepo
import com.teamdobermans.studyos.repo.FlashcardRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val repo: FlashcardRepo = FlashcardRepoImpl()
) : ViewModel() {

    private val _dueCards = MutableStateFlow<List<FlashcardModel>>(emptyList())
    val dueCards: StateFlow<List<FlashcardModel>> = _dueCards

    private val _currentCard = MutableStateFlow<FlashcardModel?>(null)
    val currentCard: StateFlow<FlashcardModel?> = _currentCard

    fun loadDueCards() {
        viewModelScope.launch {
            val due = repo.getDueFlashcards()
                .sortedBy { it.nextReviewDate }
            _dueCards.value = due
            _currentCard.value = due.firstOrNull()
        }
    }

    fun addCard(question: String, answer: String, subjectId: String = "") {
        viewModelScope.launch {
            repo.addFlashcard(
                FlashcardModel(
                    question = question,
                    answer = answer,
                    subjectId = subjectId
                )
            )
            loadDueCards()
        }
    }

    fun submitReview(card: FlashcardModel, quality: Int) {
        viewModelScope.launch {
            val newEase = maxOf(1.3f, card.easeFactor + 0.1f - (5 - quality) * 0.08f)
            val newInterval = if (quality < 3) 1 else (card.intervalDays * newEase).toInt()
            val updated = card.copy(
                intervalDays = newInterval,
                easeFactor = newEase,
                nextReviewDate = System.currentTimeMillis() + newInterval * 86400000L
            )
            repo.updateFlashcard(updated)
            loadDueCards()
        }
    }
}