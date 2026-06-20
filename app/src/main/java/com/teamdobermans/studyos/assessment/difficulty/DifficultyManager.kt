package com.teamdobermans.studyos.assessment.difficulty

import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

object DifficultyManager {

    fun preferredTypes(difficulty: Difficulty): List<QuestionType> = when (difficulty) {
        Difficulty.EASY   -> listOf(
            QuestionType.DEFINITION,
            QuestionType.FILL_IN_BLANK
        )
        Difficulty.MEDIUM -> listOf(
            QuestionType.DEFINITION,
            QuestionType.FILL_IN_BLANK,
            QuestionType.CONCEPT_RECOGNITION,
            QuestionType.RELATIONSHIP
        )
        Difficulty.HARD   -> listOf(
            QuestionType.RELATIONSHIP,
            QuestionType.CONCEPT_RECOGNITION,
            QuestionType.PROCESS,
            QuestionType.APPLICATION
        )
    }

    fun targetDistribution(count: Int, difficulty: Difficulty): Map<QuestionType, Int> {
        val types = preferredTypes(difficulty)
        if (types.isEmpty()) return emptyMap()
        val base      = count / types.size
        val remainder = count % types.size
        return types.mapIndexed { i, type -> type to (base + if (i < remainder) 1 else 0) }.toMap()
    }
}
