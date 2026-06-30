package com.teamdobermans.studyos.model

data class UserSettings(
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val dailyStudyGoalMinutes: Int = 120,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val updatedAt: Long = System.currentTimeMillis()
)
