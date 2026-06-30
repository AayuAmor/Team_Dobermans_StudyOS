package com.teamdobermans.studyos.model

data class WeeklyTrendPoint(
    val dayLabel: String,
    val dateMillis: Long,
    val value: Float
)
