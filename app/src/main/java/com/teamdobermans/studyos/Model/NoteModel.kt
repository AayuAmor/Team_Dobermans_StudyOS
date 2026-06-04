package com.teamdobermans.studyos.Model

data class NoteModel(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val folder: String = "Science",
    val timestamp: Long = 0L,
    val userId: String = ""
)