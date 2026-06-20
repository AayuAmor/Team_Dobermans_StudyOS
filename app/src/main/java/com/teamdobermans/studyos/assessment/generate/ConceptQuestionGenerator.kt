package com.teamdobermans.studyos.assessment.generate

import com.teamdobermans.studyos.assessment.model.ExtractedConcept
import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

class ConceptQuestionGenerator : QuestionGenerator {

    override val supportedTypes = listOf(QuestionType.CONCEPT_RECOGNITION)

    override fun canGenerate(analysis: NoteAnalysis): Boolean =
        analysis.concepts.size >= 4 && analysis.sentences.size >= 4

    override fun generate(analysis: NoteAnalysis, difficulty: Difficulty): List<QuestionBlueprint> {
        val topConcepts = analysis.concepts.take(10)
        val blueprints  = mutableListOf<QuestionBlueprint>()

        for (concept in topConcepts) {
            val contextSentence = findBestContextSentence(concept, analysis.sentences) ?: continue

            val distractorPool = analysis.sentences.filter { sentence ->
                !sentence.contains(concept.term, ignoreCase = true) &&
                sentence.lowercase().trim() != contextSentence.lowercase().trim()
            }.take(5)

            if (distractorPool.size < 3) continue

            blueprints.add(
                QuestionBlueprint(
                    type           = QuestionType.CONCEPT_RECOGNITION,
                    stem           = buildStem(concept.term, difficulty),
                    correctAnswer  = contextSentence,
                    distractorPool = distractorPool,
                    sourceSentence = contextSentence,
                    sourceConcept  = concept.term,
                    difficulty     = difficulty,
                    confidenceScore = (concept.importance * 0.85f).coerceAtMost(0.95f)
                )
            )
        }
        return blueprints
    }

    private fun findBestContextSentence(concept: ExtractedConcept, sentences: List<String>): String? {
        if (concept.firstContext.isNotBlank() && concept.firstContext.split(Regex("\\s+")).size >= 5)
            return concept.firstContext
        return sentences.firstOrNull { it.contains(concept.term, ignoreCase = true) }
    }

    private fun buildStem(concept: String, difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EASY   -> "Which statement about $concept is correct?"
        Difficulty.MEDIUM -> "Which of the following best describes $concept?"
        Difficulty.HARD   -> "Which statement most accurately characterises $concept?"
    }
}
