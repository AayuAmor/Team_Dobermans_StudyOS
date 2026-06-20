package com.teamdobermans.studyos.model

data class QuizModel(
    val id: String = "",
    val noteId: String = "",
    val noteTitle: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val userId: String = "",
    val questions: List<QuizQuestionModel> = emptyList()
)

data class QuizQuestionModel(
    val id: String = "",
    val quizId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String? = null,
    val difficulty: String = "MEDIUM",
    val questionType: String = "Multiple Choice",
    val sourceConcept: String = "",
    val confidenceScore: Float = 1.0f
)

data class QuizAttemptModel(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val difficulty: String = "MEDIUM",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val scorePercentage: Float = 0f,
    val durationMinutes: Int = 0,
    val completedAt: Long = 0L,
    val userId: String = "",
    val quizId: String = ""
)
