package com.teamdobermans.studyos.model

import java.time.LocalDate
import java.util.UUID

enum class Priority { HIGH, MEDIUM, LOW }

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate?, // Null if it's a single day deadline
    val priority: Priority,
    val subjectId: String,
    val subjectName: String,
    val done: Boolean = false
) {
    // Business logic checking if task is overdue relative to a given date
    fun isOverdue(compareDate: LocalDate = LocalDate.now()): Boolean {
        if (done) return false
        val finalDeadline = endDate ?: startDate
        return compareDate.isAfter(finalDeadline)
    }
}