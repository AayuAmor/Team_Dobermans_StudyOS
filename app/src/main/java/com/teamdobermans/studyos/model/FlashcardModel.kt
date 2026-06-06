package com.teamdobermans.studyos.model

data class FlashcardModel(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val subject: String = "Biology",
    val timestamp: Long = 0L,
    val userId: String = ""
)