package com.teamdobermans.studyos.model

data class FlashcardModel(
    val id: String = "",
    val userId: String = "",
    val front: String = "",
    val back: String = "",
    val deckName: String = "General",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastReviewedAt: Long = 0L,
    val reviewCount: Int = 0
)
