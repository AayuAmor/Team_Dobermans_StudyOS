package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.FlashcardModel

interface FlashcardRepo {
    suspend fun getAllFlashcards(): List<FlashcardModel>
    suspend fun getDueFlashcards(): List<FlashcardModel>
    suspend fun updateFlashcard(card: FlashcardModel)
    suspend fun addFlashcard(card: FlashcardModel)
}