package com.teamdobermans.studyos.model

import java.time.LocalDate
import java.util.UUID

enum class Priority { HIGH, MEDIUM, LOW }

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val priority: Priority,
    val subjectId: String,
    val subjectName: String,
    val done: Boolean = false,
    val linkedSessionIds: List<String> = emptyList()
) {

    fun isOverdue(compareDate: LocalDate = LocalDate.now()): Boolean {
        if (done) return false
        val finalDeadline = endDate ?: startDate
        return compareDate.isAfter(finalDeadline)
    }
}