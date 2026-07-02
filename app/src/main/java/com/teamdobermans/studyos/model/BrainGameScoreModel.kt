package com.teamdobermans.studyos.model

data class BrainGameScoreModel(
    val id: String = "",
    val userId: String = "",
    val mode: String = "",
    val score: Int = 0,
    val durationSeconds: Int = 0,
    val moves: Int? = null,
    val correctAnswers: Int? = null,
    val wrongAnswers: Int? = null,
    val accuracy: Double? = null,
    val playedAfterSession: Boolean = false,
    val relatedSessionId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
