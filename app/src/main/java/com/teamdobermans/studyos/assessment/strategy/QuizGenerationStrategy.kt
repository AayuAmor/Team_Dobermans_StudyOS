package com.teamdobermans.studyos.assessment.strategy

import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.QuizQuestionModel

sealed class GenerationResult {
    data class Success(val questions: List<QuizQuestionModel>) : GenerationResult()
    data class Failure(val reason: String) : GenerationResult()
    object InsufficientContent : GenerationResult()
}

interface QuizGenerationStrategy {
    suspend fun generate(
        note: NoteModel,
        count: Int,
        difficulty: Difficulty
    ): GenerationResult
}
