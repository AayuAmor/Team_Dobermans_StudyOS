package com.teamdobermans.studyos.assessment.assemble

import com.teamdobermans.studyos.assessment.difficulty.DifficultyManager
import com.teamdobermans.studyos.assessment.distract.DistractorGenerator
import com.teamdobermans.studyos.assessment.generate.ConceptQuestionGenerator
import com.teamdobermans.studyos.assessment.generate.DefinitionQuestionGenerator
import com.teamdobermans.studyos.assessment.generate.FillBlankQuestionGenerator
import com.teamdobermans.studyos.assessment.generate.QuestionGenerator
import com.teamdobermans.studyos.assessment.generate.RelationshipQuestionGenerator
import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.assessment.validate.QualityValidator
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.QuizQuestionModel
import java.util.UUID

class QuizAssembler(
    private val generators: List<QuestionGenerator> = listOf(
        DefinitionQuestionGenerator(),
        FillBlankQuestionGenerator(),
        ConceptQuestionGenerator(),
        RelationshipQuestionGenerator()
    ),
    private val distractorGenerator: DistractorGenerator = DistractorGenerator(),
    private val validator: QualityValidator = QualityValidator()
) {

    fun assemble(analysis: NoteAnalysis, count: Int, difficulty: Difficulty): List<QuizQuestionModel> {
        val targetDist = DifficultyManager.targetDistribution(count, difficulty)

        val allBlueprints = generators
            .filter { it.canGenerate(analysis) }
            .flatMap { it.generate(analysis, difficulty) }
            .sortedByDescending { it.confidenceScore }

        val selected    = mutableListOf<QuizQuestionModel>()
        val typeCount   = mutableMapOf<QuestionType, Int>()
        val seenSources = mutableSetOf<String>()

        for (blueprint in allBlueprints) {
            if (selected.size >= count) break
            if (blueprint.sourceConcept in seenSources) continue

            val typeTarget  = targetDist[blueprint.type] ?: (count / 4).coerceAtLeast(1)
            val typeCurrent = typeCount[blueprint.type] ?: 0
            if (typeCurrent >= typeTarget && selected.size + (count - selected.size) > 0) {
                if (allBlueprints.none { it.type != blueprint.type && it.sourceConcept !in seenSources }) {
                    // no other types left; keep filling with this type
                } else if (typeCurrent >= typeTarget) continue
            }

            val distractors = distractorGenerator.generate(blueprint, analysis, 3)
            if (distractors.size < 3) continue

            val options = (listOf(blueprint.correctAnswer) + distractors).shuffled()
            val question = QuizQuestionModel(
                id             = UUID.randomUUID().toString(),
                quizId         = "",
                question       = blueprint.stem,
                options        = options,
                correctAnswer  = blueprint.correctAnswer,
                explanation    = buildExplanation(blueprint),
                difficulty     = blueprint.difficulty.name,
                questionType   = blueprint.type.label,
                sourceConcept  = blueprint.sourceConcept,
                confidenceScore = blueprint.confidenceScore
            )

            val validationResult = validator.validate(question)
            if (!validationResult.isValid) continue

            selected.add(question)
            typeCount[blueprint.type] = typeCurrent + 1
            seenSources.add(blueprint.sourceConcept)
        }

        return validator.deduplicateQuestions(selected)
    }

    private fun buildExplanation(blueprint: QuestionBlueprint): String? {
        val src = blueprint.sourceSentence.trim()
        if (src.isBlank()) return null
        return "From your notes: \"${src.take(160)}${if (src.length > 160) "…" else ""}\""
    }
}
