package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.assessment.QuizGenerationEngine
import com.teamdobermans.studyos.assessment.analyze.NoteAnalyzer
import com.teamdobermans.studyos.assessment.strategy.GenerationResult
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.NoteReadinessInfo
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizSetupUiState
import com.teamdobermans.studyos.model.QuizTypeOption
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

class MockTestViewModel : ViewModel() {

    private val noteRepo         = NoteRepoImpl()
    private val quizRepo         = QuizRepoImpl()
    private val generationEngine = QuizGenerationEngine()
    private val noteAnalyzer     = NoteAnalyzer()

    private val _notesLoading = MutableStateFlow(true)
    val notesLoading: StateFlow<Boolean> = _notesLoading.asStateFlow()

    val notes: StateFlow<List<NoteModel>> = noteRepo.getNotes()
        .onEach { _notesLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val history: StateFlow<List<QuizAttemptModel>> = quizRepo.observeAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _questionCount = MutableStateFlow(5)
    val questionCount: StateFlow<Int> = _questionCount.asStateFlow()

    private val _difficulty = MutableStateFlow(Difficulty.MEDIUM)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    private val _questionType = MutableStateFlow(QuizTypeOption.MIXED)
    val questionType: StateFlow<QuizTypeOption> = _questionType.asStateFlow()

    private val _uiState = MutableStateFlow<QuizSetupUiState>(QuizSetupUiState.NoteSelection)
    val uiState: StateFlow<QuizSetupUiState> = _uiState.asStateFlow()

    private var quizStartEpoch: Long = 0L

    fun preSelectNote(noteId: String, noteTitle: String, noteBody: String) {
        selectNote(NoteModel(id = noteId, title = noteTitle, body = noteBody))
    }

    fun selectNote(note: NoteModel) {
        _uiState.value = QuizSetupUiState.Setup(note = note, readiness = buildReadiness(note))
    }

    fun setQuestionCount(count: Int) { _questionCount.value = count }
    fun setDifficulty(d: Difficulty) { _difficulty.value = d }
    fun setQuestionType(t: QuizTypeOption) { _questionType.value = t }

    fun generateQuiz() {
        val setup = _uiState.value as? QuizSetupUiState.Setup ?: return
        val note  = setup.note
        _uiState.value = QuizSetupUiState.Generating(note)

        viewModelScope.launch {
            when (val result = generationEngine.generate(note, _questionCount.value, _difficulty.value)) {
                is GenerationResult.Success -> {
                    if (result.questions.isEmpty()) {
                        _uiState.value = QuizSetupUiState.Error(
                            "Could not generate enough questions from this note.\n\n" +
                            "Add more definitions, explanations, and descriptions.",
                            note
                        )
                    } else {
                        quizStartEpoch = System.currentTimeMillis()
                        _uiState.value = QuizSetupUiState.Playing(note = note, questions = result.questions)
                    }
                }
                is GenerationResult.InsufficientContent -> {
                    _uiState.value = QuizSetupUiState.Error(
                        "This note does not contain enough content to generate a reliable quiz.\n\n" +
                        "Add definitions, explanations, and detailed descriptions to your notes.\n\n" +
                        "Example: \"A stack is a LIFO data structure used to store elements.\"",
                        note
                    )
                }
                is GenerationResult.Failure -> {
                    _uiState.value = QuizSetupUiState.Error(result.reason, note)
                }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answer: String) {
        val s = _uiState.value as? QuizSetupUiState.Playing ?: return
        _uiState.value = s.copy(userAnswers = s.userAnswers + (questionIndex to answer))
    }

    fun nextQuestion() {
        val s = _uiState.value as? QuizSetupUiState.Playing ?: return
        if (s.currentIndex < s.questions.size - 1)
            _uiState.value = s.copy(currentIndex = s.currentIndex + 1)
    }

    fun previousQuestion() {
        val s = _uiState.value as? QuizSetupUiState.Playing ?: return
        if (s.currentIndex > 0)
            _uiState.value = s.copy(currentIndex = s.currentIndex - 1)
    }

    fun submitQuiz() {
        val s         = _uiState.value as? QuizSetupUiState.Playing ?: return
        val questions = s.questions
        val answers   = s.userAnswers

        var correct = 0
        for ((idx, q) in questions.withIndex()) {
            if ((answers[idx] ?: "").equals(q.correctAnswer, ignoreCase = true)) correct++
        }

        val total       = questions.size
        val pct         = if (total > 0) (correct.toFloat() / total) * 100f else 0f
        val elapsedMins = ((System.currentTimeMillis() - quizStartEpoch) / 60000L).toInt().coerceAtLeast(1)

        val attempt = QuizAttemptModel(
            id              = UUID.randomUUID().toString(),
            title           = "Quiz: ${s.note.title}",
            subject         = s.note.title,
            difficulty      = _difficulty.value.name,
            totalQuestions  = total,
            correctAnswers  = correct,
            scorePercentage = pct,
            durationMinutes = elapsedMins,
            completedAt     = System.currentTimeMillis()
        )

        viewModelScope.launch { quizRepo.saveAttempt(attempt) }

        _uiState.value = QuizSetupUiState.Result(
            note        = s.note,
            attempt     = attempt,
            questions   = questions,
            userAnswers = answers
        )
    }

    fun retakeQuiz() {
        val s = _uiState.value as? QuizSetupUiState.Result ?: return
        val note = s.note
        _uiState.value = QuizSetupUiState.Generating(note)

        viewModelScope.launch {
            when (val result = generationEngine.generate(note, _questionCount.value, _difficulty.value)) {
                is GenerationResult.Success -> {
                    if (result.questions.isNotEmpty()) {
                        quizStartEpoch = System.currentTimeMillis()
                        _uiState.value = QuizSetupUiState.Playing(note = note, questions = result.questions)
                    } else {
                        _uiState.value = QuizSetupUiState.Setup(note = note, readiness = buildReadiness(note))
                    }
                }
                else -> {
                    _uiState.value = QuizSetupUiState.Setup(note = note, readiness = buildReadiness(note))
                }
            }
        }
    }

    fun backToSetup() {
        val note = when (val s = _uiState.value) {
            is QuizSetupUiState.Generating -> s.note
            is QuizSetupUiState.Playing    -> s.note
            is QuizSetupUiState.Result     -> s.note
            is QuizSetupUiState.Error      -> s.note
            else                           -> null
        }
        _uiState.value = if (note != null)
            QuizSetupUiState.Setup(note = note, readiness = buildReadiness(note))
        else
            QuizSetupUiState.NoteSelection
    }

    fun showHistory() { _uiState.value = QuizSetupUiState.History }

    fun navigateToNoteSelection() { _uiState.value = QuizSetupUiState.NoteSelection }

    private fun buildReadiness(note: NoteModel): NoteReadinessInfo {
        val analysis  = noteAnalyzer.analyze(note.title, note.body)
        val wordCount = note.body.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val estimatedQ = (analysis.definitions.size + analysis.relationships.size +
                         analysis.concepts.size / 3).coerceAtLeast(0)
        val quality = when {
            analysis.definitions.size >= 5                         -> "Excellent"
            analysis.definitions.size >= 3                         -> "Good"
            analysis.definitions.size >= 1 && wordCount >= 50     -> "Fair"
            else                                                   -> "Insufficient"
        }
        return NoteReadinessInfo(
            wordCount         = wordCount,
            sentenceCount     = analysis.sentences.size,
            estimatedQuestions = estimatedQ,
            hasEnoughContent  = analysis.isSubstantial,
            qualityLabel      = quality
        )
    }
}
