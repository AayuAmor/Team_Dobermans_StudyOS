package com.teamdobermans.studyos.model

data class NoteModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val folder: String = "",
    val timestamp: Long = 0L,
    val updatedAt: Long = 0L,
    val sourceType: String = "MANUAL",
    val userId: String = "",

    // Spaced-repetition review fields
    val reminderEnabled: Boolean = false,
    val reviewStage: Int = 0,       // 0=created, 1=day1 done, 2=day4 done, 3=day7 done
    val createdAt: Long = 0L,
    val lastReviewedAt: Long = 0L,  // 0 = never reviewed
    val nextReviewAt: Long = 0L     // 0 = no upcoming review
)