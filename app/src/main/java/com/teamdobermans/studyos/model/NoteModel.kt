package com.teamdobermans.studyos.model

data class NoteModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val folder: String = "Science",
    val timestamp: Long = 0L,
    val updatedAt: Long = 0L,
    val sourceType: String = "MANUAL",
    val userId: String = ""
)
