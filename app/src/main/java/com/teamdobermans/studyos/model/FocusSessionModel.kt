package com.teamdobermans.studyos.model

data class FocusSessionModel(
    val id: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val durationMinutes: Int = 25,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val userId: String = ""
)