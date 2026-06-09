package com.teamdobermans.studyos.model

data class UserSettings(
    val remindersEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
