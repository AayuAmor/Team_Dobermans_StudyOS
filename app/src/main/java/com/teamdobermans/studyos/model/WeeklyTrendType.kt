package com.teamdobermans.studyos.model

enum class WeeklyTrendType(val label: String, val unit: String) {
    STUDY_TIME("Study Time", "min"),
    TASKS_COMPLETED("Tasks", "done"),
    QUIZ_SCORE("Quiz Score", "%"),
    REVIEW_COUNT("Reviews", "notes")
}
