package com.teamdobermans.studyos.model

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val offlineMode: Boolean = true,
    val weeklySummary: Boolean = true,
    val focusSounds: Boolean = false,
    val pinNotes: Boolean = false,
    val dailyGoalMin: Int = 60,
    val pomodoroDuration: Int = 25
)