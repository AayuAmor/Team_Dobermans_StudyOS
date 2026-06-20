package com.teamdobermans.studyos.assessment.generate

import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

class FillBlankQuestionGenerator : QuestionGenerator {

    override val supportedTypes = listOf(QuestionType.FILL_IN_BLANK)

    override fun canGenerate(analysis: NoteAnalysis): Boolean =
        analysis.definitions.isNotEmpty() || analysis.concepts.size >= 3

    override fun generate(analysis: NoteAnalysis, difficulty: Difficulty): List<QuestionBlueprint> {
        val blueprints = mutableListOf<QuestionBlueprint>()
        val usedConcepts = mutableSetOf<String>()

        for (def in analysis.definitions) {
            val blanked = blank(def.sourceSentence, def.term) ?: continue
            val distractorPool = analysis.allTerms
                .filter { it.lowercase() != def.term.lowercase() }

            if (distractorPool.size < 3) continue
            usedConcepts.add(def.term.lowercase())

            blueprints.add(
                QuestionBlueprint(
                    type           = QuestionType.FILL_IN_BLANK,
                    stem           = blanked,
                    correctAnswer  = def.term,
                    distractorPool = distractorPool,
                    sourceSentence = def.sourceSentence,
                    sourceConcept  = def.term,
                    difficulty     = difficulty,
                    confidenceScore = 0.80f
                )
            )
        }

        for (concept in analysis.concepts.take(12)) {
            if (concept.term.lowercase() in usedConcepts) continue

            val contextSentence = analysis.sentences.firstOrNull { s ->
                s.contains(concept.term, ignoreCase = true)
            } ?: continue

            val blanked = blank(contextSentence, concept.term) ?: continue

            val distractorPool = analysis.concepts
                .filter { it.term.lowercase() != concept.term.lowercase() }
                .map { it.term }

            if (distractorPool.size < 3) continue
            usedConcepts.add(concept.term.lowercase())

            blueprints.add(
                QuestionBlueprint(
                    type           = QuestionType.FILL_IN_BLANK,
                    stem           = blanked,
                    correctAnswer  = concept.term,
                    distractorPool = distractorPool,
                    sourceSentence = contextSentence,
                    sourceConcept  = concept.term,
                    difficulty     = difficulty,
                    confidenceScore = 0.60f + (concept.importance * 0.15f)
                )
            )
        }

        return blueprints
    }

    private fun blank(sentence: String, term: String): String? {
        val pattern = if (term.contains(" ")) {
            Regex(Regex.escape(term), RegexOption.IGNORE_CASE)
        } else {
            Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
        }
        val result = sentence.replaceFirst(pattern, "___________")
        return if (result.contains("___________")) result else null
    }
}
