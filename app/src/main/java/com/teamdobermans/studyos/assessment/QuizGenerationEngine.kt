package com.teamdobermans.studyos.assessment

import com.teamdobermans.studyos.assessment.analyze.NoteAnalyzer
import com.teamdobermans.studyos.assessment.assemble.QuizAssembler
import com.teamdobermans.studyos.assessment.strategy.GenerationResult
import com.teamdobermans.studyos.assessment.strategy.QuizGenerationStrategy
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.QuizQuestionModel

class QuizGenerationEngine(
    private val noteAnalyzer: NoteAnalyzer = NoteAnalyzer(),
    private val assembler: QuizAssembler = QuizAssembler()
) : QuizGenerationStrategy {

    override suspend fun generate(
        note: NoteModel,
        count: Int,
        difficulty: Difficulty
    ): GenerationResult {
        if (note.body.isBlank()) return GenerationResult.InsufficientContent

        val analysis = noteAnalyzer.analyze(note.title, note.body)

        if (!analysis.isSubstantial) {
            return GenerationResult.InsufficientContent
        }

        val questions = assembler.assemble(analysis, count, difficulty)

        return if (questions.isEmpty()) {
            GenerationResult.Failure(
                "Not enough structured content in \"${note.title}\" to generate questions. " +
                "Add definitions, explanations, and descriptions to your notes."
            )
        } else {
            GenerationResult.Success(questions)
        }
    }

    fun generateSync(
        note: NoteModel,
        count: Int = 5,
        difficulty: Difficulty = Difficulty.MEDIUM
    ): List<QuizQuestionModel> {
        if (note.body.isBlank()) return emptyList()
        val analysis = noteAnalyzer.analyze(note.title, note.body)
        if (!analysis.isSubstantial) return emptyList()
        return assembler.assemble(analysis, count, difficulty)
    }
}
