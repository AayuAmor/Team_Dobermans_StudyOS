package com.teamdobermans.studyos.assessment.generate

import com.teamdobermans.studyos.assessment.model.ExtractedDefinition
import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

class DefinitionQuestionGenerator : QuestionGenerator {

    override val supportedTypes = listOf(QuestionType.DEFINITION)

    override fun canGenerate(analysis: NoteAnalysis): Boolean =
        analysis.definitions.size >= 2

    override fun generate(analysis: NoteAnalysis, difficulty: Difficulty): List<QuestionBlueprint> {
        val definitions = analysis.definitions
        if (definitions.size < 2) return emptyList()

        return definitions.mapIndexed { idx, def ->
            val distractorPool = definitions
                .filterIndexed { i, _ -> i != idx }
                .map { it.definition }

            QuestionBlueprint(
                type           = QuestionType.DEFINITION,
                stem           = buildStem(def, difficulty),
                correctAnswer  = def.definition,
                distractorPool = distractorPool,
                sourceSentence = def.sourceSentence,
                sourceConcept  = def.term,
                difficulty     = difficulty,
                confidenceScore = computeConfidence(def, distractorPool.size)
            )
        }
    }

    private fun buildStem(def: ExtractedDefinition, difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EASY   -> "What is ${def.term}?"
        Difficulty.MEDIUM -> "Which of the following best describes ${def.term}?"
        Difficulty.HARD   -> "Which statement about ${def.term} is most accurate?"
    }

    private fun computeConfidence(def: ExtractedDefinition, distractorCount: Int): Float {
        var score = 0.5f
        if (distractorCount >= 3) score += 0.3f
        if (def.definition.length in 8..150) score += 0.1f
        if (def.term.length in 3..30) score += 0.1f
        return score.coerceAtMost(1.0f)
    }
}
