package com.teamdobermans.studyos.assessment.validate

import com.teamdobermans.studyos.model.QuizQuestionModel

class QualityValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val reason: String? = null
    )

    fun validate(q: QuizQuestionModel): ValidationResult {
        if (q.question.isBlank())
            return invalid("Empty question stem")

        if (q.question.length < 10)
            return invalid("Question too short (${q.question.length} chars)")

        if (q.options.size < 4)
            return invalid("Fewer than 4 options (${q.options.size})")

        if (q.correctAnswer.isBlank())
            return invalid("Empty correct answer")

        if (q.options.none { it == q.correctAnswer })
            return invalid("Correct answer not in options list")

        if (q.options.any { it.isBlank() })
            return invalid("One or more blank options")

        val lowerOptions = q.options.map { it.lowercase().trim() }
        if (lowerOptions.distinct().size != q.options.size)
            return invalid("Duplicate options detected")

        if (q.options.any { it.length > 400 })
            return invalid("Option too long (> 400 chars)")

        val isSentenceType = q.questionType in listOf(
            "Concept Recognition", "Relationship", "Process", "Application"
        )
        if (!isSentenceType) {
            val longest  = q.options.maxOf { it.length }.toFloat()
            val shortest = q.options.minOf { it.length }.toFloat()
            if (longest > 0 && shortest / longest < 0.08f)
                return invalid("Option length ratio too extreme — obvious answer")
        }

        return ValidationResult(true)
    }

    fun deduplicateQuestions(questions: List<QuizQuestionModel>): List<QuizQuestionModel> {
        val seenStems   = mutableSetOf<String>()
        val seenAnswers = mutableSetOf<String>()
        return questions.filter { q ->
            val stemKey   = q.question.lowercase().trim()
            val answerKey = "${q.sourceConcept}:${q.correctAnswer.take(30).lowercase()}"
            if (stemKey in seenStems || answerKey in seenAnswers) {
                false
            } else {
                seenStems.add(stemKey)
                seenAnswers.add(answerKey)
                true
            }
        }
    }

    private fun invalid(reason: String) = ValidationResult(false, reason)
}
