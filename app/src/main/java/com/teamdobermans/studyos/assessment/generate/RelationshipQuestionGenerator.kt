package com.teamdobermans.studyos.assessment.generate

import com.teamdobermans.studyos.assessment.model.ExtractedRelationship
import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

class RelationshipQuestionGenerator : QuestionGenerator {

    override val supportedTypes = listOf(QuestionType.RELATIONSHIP)

    override fun canGenerate(analysis: NoteAnalysis): Boolean =
        analysis.relationships.size >= 2

    override fun generate(analysis: NoteAnalysis, difficulty: Difficulty): List<QuestionBlueprint> {
        val relationships = analysis.relationships
        if (relationships.size < 2) return emptyList()

        return relationships.mapIndexed { idx, rel ->
            val distractorPool = buildDistractorPool(rel, relationships, idx, analysis)
            val confidence = if (distractorPool.size >= 3) 0.80f else 0.45f

            QuestionBlueprint(
                type           = QuestionType.RELATIONSHIP,
                stem           = buildStem(rel, difficulty),
                correctAnswer  = rel.sourceSentence,
                distractorPool = distractorPool,
                sourceSentence = rel.sourceSentence,
                sourceConcept  = "${rel.subject} — ${rel.objectTerm}",
                difficulty     = difficulty,
                confidenceScore = confidence
            )
        }
    }

    private fun buildDistractorPool(
        rel: ExtractedRelationship,
        allRelationships: List<ExtractedRelationship>,
        currentIdx: Int,
        analysis: NoteAnalysis
    ): List<String> {
        val fromRelationships = allRelationships
            .filterIndexed { i, _ -> i != currentIdx }
            .map { it.sourceSentence }

        val fromSentences = analysis.sentences.filter { sentence ->
            !sentence.contains(rel.subject, ignoreCase = true) &&
            sentence.lowercase().trim() != rel.sourceSentence.lowercase().trim()
        }.take(3)

        return (fromRelationships + fromSentences)
            .distinctBy { it.lowercase().trim() }
    }

    private fun buildStem(rel: ExtractedRelationship, difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EASY   -> "Which statement describes how ${rel.subject} relates to ${rel.objectTerm}?"
        Difficulty.MEDIUM -> "How does ${rel.subject} ${rel.predicate} ${rel.objectTerm}?"
        Difficulty.HARD   -> "Which statement most accurately explains the relationship between ${rel.subject} and ${rel.objectTerm}?"
    }
}
