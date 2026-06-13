package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.FlashcardModel
import kotlinx.coroutines.flow.Flow

interface FlashcardRepo {
    fun observeFlashcards(deckName: String? = null): Flow<List<FlashcardModel>>
    suspend fun createFlashcard(front: String, back: String, deckName: String): String?
    suspend fun updateFlashcard(card: FlashcardModel): Boolean
    suspend fun deleteFlashcard(cardId: String)
    suspend fun recordReview(cardId: String)
}