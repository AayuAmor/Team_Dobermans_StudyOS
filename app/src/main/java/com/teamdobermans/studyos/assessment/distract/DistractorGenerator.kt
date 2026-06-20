package com.teamdobermans.studyos.assessment.distract

import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType

class DistractorGenerator {

    fun generate(blueprint: QuestionBlueprint, analysis: NoteAnalysis, needed: Int = 3): List<String> {
        val candidates = when (blueprint.type) {
            QuestionType.DEFINITION          -> forDefinition(blueprint, analysis)
            QuestionType.FILL_IN_BLANK       -> forFillBlank(blueprint, analysis)
            QuestionType.CONCEPT_RECOGNITION -> forConcept(blueprint, analysis)
            QuestionType.RELATIONSHIP        -> forRelationship(blueprint, analysis)
            QuestionType.PROCESS             -> forProcess(blueprint, analysis)
            QuestionType.APPLICATION         -> forConcept(blueprint, analysis)
        }

        val correctLower = blueprint.correctAnswer.lowercase().trim()
        return candidates
            .filter { it.isNotBlank() }
            .filter { it.lowercase().trim() != correctLower }
            .filter { it.length > 2 }
            .distinctBy { it.lowercase().trim() }
            .take(needed)
    }

    private fun forDefinition(blueprint: QuestionBlueprint, analysis: NoteAnalysis): List<String> {
        val fromPool = blueprint.distractorPool.filter { it.isNotBlank() }

        val fromConceptContexts = analysis.concepts
            .filter { it.term.lowercase() != blueprint.sourceConcept.lowercase() }
            .filter { it.firstContext.isNotBlank() }
            .map { it.firstContext }
            .take(4)

        return (fromPool + fromConceptContexts).distinctBy { it.lowercase().take(40) }
    }

    private fun forFillBlank(blueprint: QuestionBlueprint, analysis: NoteAnalysis): List<String> {
        val fromPool = blueprint.distractorPool.filter { it.isNotBlank() }

        val fromDefinitions = analysis.definitions
            .map { it.term }
            .filter { it.lowercase() != blueprint.correctAnswer.lowercase() }

        val fromConcepts = analysis.concepts
            .map { it.term }
            .filter { it.lowercase() != blueprint.correctAnswer.lowercase() }

        return (fromPool + fromDefinitions + fromConcepts).distinctBy { it.lowercase() }
    }

    private fun forConcept(blueprint: QuestionBlueprint, analysis: NoteAnalysis): List<String> {
        val correctLower = blueprint.correctAnswer.lowercase().trim()

        val fromPool = blueprint.distractorPool.filter { sentence ->
            sentence.isNotBlank() && sentence.lowercase().trim() != correctLower
        }

        val fromSentences = analysis.sentences.filter { sentence ->
            !sentence.contains(blueprint.sourceConcept, ignoreCase = true) &&
            sentence.lowercase().trim() != correctLower
        }.take(5)

        return (fromPool + fromSentences).distinctBy { it.lowercase().take(50) }
    }

    private fun forRelationship(blueprint: QuestionBlueprint, analysis: NoteAnalysis): List<String> {
        val correctLower = blueprint.correctAnswer.lowercase().trim()
        val subjectParts = blueprint.sourceConcept.split("—").map { it.trim() }
        val primarySubject = subjectParts.firstOrNull() ?: ""

        val fromPool = blueprint.distractorPool.filter { it.isNotBlank() && it.lowercase().trim() != correctLower }

        val fromOtherSentences = analysis.sentences.filter { sentence ->
            sentence.lowercase().trim() != correctLower &&
            !sentence.contains(primarySubject, ignoreCase = true)
        }.take(3)

        return (fromPool + fromOtherSentences).distinctBy { it.lowercase().take(50) }
    }

    private fun forProcess(blueprint: QuestionBlueprint, analysis: NoteAnalysis): List<String> {
        val correctLower = blueprint.correctAnswer.lowercase().trim()
        return (blueprint.distractorPool + analysis.sentences)
            .filter { it.lowercase().trim() != correctLower && it.isNotBlank() }
            .distinctBy { it.lowercase().take(50) }
    }
}
