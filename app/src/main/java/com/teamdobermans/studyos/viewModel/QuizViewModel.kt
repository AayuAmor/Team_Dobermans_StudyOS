package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.assessment.QuizGenerationEngine
import com.teamdobermans.studyos.assessment.strategy.GenerationResult
import com.teamdobermans.studyos.assessment.strategy.QuizGenerationStrategy
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizModel
import com.teamdobermans.studyos.model.QuizQuestionModel
import com.teamdobermans.studyos.repo.NoteRepoImpl
import com.teamdobermans.studyos.repo.QuizRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface QuizFlowState {
    object Idle : QuizFlowState
    object GeneratingQuiz : QuizFlowState
    data class Playing(
        val quiz: QuizModel,
        val currentIndex: Int = 0,
        val userAnswers: Map<Int, String> = emptyMap()
    ) : QuizFlowState
    data class Result(
        val quiz: QuizModel,
        val attempt: QuizAttemptModel,
        val questions: List<QuizQuestionModel>,
        val userAnswers: Map<Int, String>
    ) : QuizFlowState
    data class Error(val message: String) : QuizFlowState
}

class QuizViewModel(
    private val generationStrategy: QuizGenerationStrategy = QuizGenerationEngine()
) : ViewModel() {

    private val noteRepo = NoteRepoImpl()
    private val quizRepo = QuizRepoImpl()

    private val _notesLoading = MutableStateFlow(true)
    val notesLoading: StateFlow<Boolean> = _notesLoading.asStateFlow()

    val notes: StateFlow<List<NoteModel>> = noteRepo.getNotes()
        .onEach { _notesLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val attempts: StateFlow<List<QuizAttemptModel>> = quizRepo.observeAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quizState = MutableStateFlow<QuizFlowState>(QuizFlowState.Idle)
    val quizState: StateFlow<QuizFlowState> = _quizState.asStateFlow()

    fun generateQuiz(note: NoteModel, difficulty: Difficulty = Difficulty.MEDIUM) {
        if (note.body.isBlank()) {
            _quizState.value = QuizFlowState.Error(
                "\"${note.title}\" has no content.\n\nAdd notes to this topic first, then generate a quiz."
            )
            return
        }
        _quizState.value = QuizFlowState.GeneratingQuiz
        viewModelScope.launch {
            when (val result = generationStrategy.generate(note, count = 5, difficulty = difficulty)) {
                is GenerationResult.Success -> {
                    val rawQuestions = result.questions
                    if (rawQuestions.isEmpty()) {
                        _quizState.value = QuizFlowState.Error(
                            "Not enough content in \"${note.title}\" to generate a quiz.\n\n" +
                            "Try writing more detailed notes with definitions and explanations."
                        )
                        return@launch
                    }
                    val quizId    = UUID.randomUUID().toString()
                    val questions = rawQuestions.map { it.copy(quizId = quizId) }
                    val quiz = QuizModel(
                        id        = quizId,
                        noteId    = note.id,
                        noteTitle = note.title,
                        title     = "Quiz: ${note.title}",
                        createdAt = System.currentTimeMillis(),
                        questions = questions
                    )
                    quizRepo.saveQuiz(quiz)
                    _quizState.value = QuizFlowState.Playing(quiz = quiz)
                }
                is GenerationResult.InsufficientContent -> {
                    _quizState.value = QuizFlowState.Error(
                        "\"${note.title}\" doesn't have enough structured content to generate a quiz.\n\n" +
                        "Try adding definitions, explanations, and detailed descriptions to your notes.\n\n" +
                        "Example: \"A stack is a LIFO data structure used to store elements.\""
                    )
                }
                is GenerationResult.Failure -> {
                    _quizState.value = QuizFlowState.Error(result.reason)
                }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answer: String) {
        val s = _quizState.value as? QuizFlowState.Playing ?: return
        _quizState.value = s.copy(userAnswers = s.userAnswers + (questionIndex to answer))
    }

    fun nextQuestion() {
        val s = _quizState.value as? QuizFlowState.Playing ?: return
        if (s.currentIndex < s.quiz.questions.size - 1) {
            _quizState.value = s.copy(currentIndex = s.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val s = _quizState.value as? QuizFlowState.Playing ?: return
        if (s.currentIndex > 0) {
            _quizState.value = s.copy(currentIndex = s.currentIndex - 1)
        }
    }

    fun submitQuiz() {
        val s = _quizState.value as? QuizFlowState.Playing ?: return
        val questions = s.quiz.questions
        val answers   = s.userAnswers

        var correct = 0
        for ((idx, q) in questions.withIndex()) {
            if ((answers[idx] ?: "").equals(q.correctAnswer, ignoreCase = true)) correct++
        }

        val total = questions.size
        val pct   = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val attempt = QuizAttemptModel(
            id              = UUID.randomUUID().toString(),
            quizId          = s.quiz.id,
            title           = s.quiz.title,
            subject         = s.quiz.noteTitle,
            correctAnswers  = correct,
            totalQuestions  = total,
            scorePercentage = pct,
            completedAt     = System.currentTimeMillis()
        )

        viewModelScope.launch { quizRepo.saveAttempt(attempt) }

        _quizState.value = QuizFlowState.Result(
            quiz        = s.quiz,
            attempt     = attempt,
            questions   = questions,
            userAnswers = answers
        )
    }

    fun retakeQuiz() {
        val s = _quizState.value as? QuizFlowState.Result ?: return
        _quizState.value = QuizFlowState.Playing(
            quiz         = s.quiz,
            currentIndex = 0,
            userAnswers  = emptyMap()
        )
    }

    fun reset() {
        _quizState.value = QuizFlowState.Idle
    }
}
