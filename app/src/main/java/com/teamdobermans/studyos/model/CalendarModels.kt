package com.teamdobermans.studyos.model

import java.time.LocalDate
import java.time.LocalTime

enum class CalendarViewMode { MONTH, WEEK }

enum class CalendarEventType { TASK, SESSION }

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val type: CalendarEventType,
    val durationMinutes: Int? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
)