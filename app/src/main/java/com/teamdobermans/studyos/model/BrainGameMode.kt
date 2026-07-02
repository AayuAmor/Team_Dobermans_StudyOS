package com.teamdobermans.studyos.model

enum class BrainGameMode {
    MEMORY_MATCH,
    MATH_SPRINT;

    companion object {
        fun fromRoute(route: String?): BrainGameMode? = when (route?.lowercase()) {
            "memory_match", "memory-match", "memory" -> MEMORY_MATCH
            "math_sprint", "math-sprint", "math" -> MATH_SPRINT
            else -> null
        }
    }
}
