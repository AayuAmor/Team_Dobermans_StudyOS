package com.teamdobermans.studyos.model

import java.util.UUID

data class Subject(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class StudySession(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val durationMillis: Long = 0L
) {
    val isActive: Boolean get() = endTimeMillis == null
}

data class StudyTimeState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: Subject? = null,
    val newSubjectName: String = "",
    val showAddSubjectDialog: Boolean = false,
    val isRunning: Boolean = false,
    val activeSession: StudySession? = null,
    val elapsedMillis: Long = 0L,
    val sessions: List<StudySession> = emptyList()
)
