package com.teamdobermans.studyos.model

data class FlashcardModel(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val nextReviewDate: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val subjectId: String = "",
    val noteId: String = "",
    val noteTitle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val mastered: Boolean = false
)
