package com.teamdobermans.studyos.model

enum class Difficulty { EASY, MEDIUM, HARD }

enum class QuizTypeOption(val label: String) {
    MULTIPLE_CHOICE("Multiple Choice"),
    FILL_IN_BLANK("Fill in the Blank"),
    MIXED("Mixed")
}

data class NoteReadinessInfo(
    val wordCount: Int,
    val sentenceCount: Int,
    val estimatedQuestions: Int,
    val hasEnoughContent: Boolean,
    val qualityLabel: String
)

sealed interface QuizSetupUiState {
    object NoteSelection : QuizSetupUiState

    data class Setup(
        val note: NoteModel,
        val readiness: NoteReadinessInfo
    ) : QuizSetupUiState

    data class Generating(val note: NoteModel) : QuizSetupUiState

    data class Playing(
        val note: NoteModel,
        val questions: List<QuizQuestionModel>,
        val currentIndex: Int = 0,
        val userAnswers: Map<Int, String> = emptyMap()
    ) : QuizSetupUiState

    data class Result(
        val note: NoteModel,
        val attempt: QuizAttemptModel,
        val questions: List<QuizQuestionModel>,
        val userAnswers: Map<Int, String>
    ) : QuizSetupUiState

    object History : QuizSetupUiState

    data class Error(
        val message: String,
        val note: NoteModel? = null
    ) : QuizSetupUiState
}
