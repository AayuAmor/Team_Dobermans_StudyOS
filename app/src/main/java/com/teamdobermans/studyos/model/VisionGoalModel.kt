package com.teamdobermans.studyos.model

data class VisionGoalModel(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val emoji: String = "🏅",
    val targetValue: String = "",
    val subject: String = "General"
)