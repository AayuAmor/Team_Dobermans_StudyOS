package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.BrainGameMode
import com.teamdobermans.studyos.model.BrainGameScoreModel
import com.teamdobermans.studyos.repo.BrainGameRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrainGameUiState(
    val mode: BrainGameMode = BrainGameMode.MEMORY_MATCH,
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeRemainingSeconds: Int = 30,
    val elapsedSeconds: Int = 0,
    val moves: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val errorMessage: String? = null,
    val isSavingScore: Boolean = false
)

class BrainGameViewModel(
    private val repository: BrainGameRepositoryImpl = BrainGameRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrainGameUiState())
    val uiState: StateFlow<BrainGameUiState> = _uiState.asStateFlow()

    private var sessionId: String? = null
    private var playedAfterSession: Boolean = false
    private var lastSavedBestScore: Int = 0

    fun startGame(mode: BrainGameMode, relatedSessionId: String? = null, playedAfterSession: Boolean = false) {
        sessionId = relatedSessionId
        this.playedAfterSession = playedAfterSession
        _uiState.value = BrainGameUiState(
            mode = mode,
            isPlaying = true,
            isCompleted = false,
            score = 0,
            bestScore = lastSavedBestScore,
            timeRemainingSeconds = if (mode == BrainGameMode.MATH_SPRINT) 30 else 0,
            elapsedSeconds = 0,
            moves = 0,
            correctAnswers = 0,
            wrongAnswers = 0,
            errorMessage = null,
            isSavingScore = false
        )
        if (mode == BrainGameMode.MATH_SPRINT) {
            startMathSprintTimer()
        }
    }

    fun onMemoryMatchMove(moved: Boolean) {
        if (_uiState.value.mode != BrainGameMode.MEMORY_MATCH || !_uiState.value.isPlaying) return
        val current = _uiState.value
        val nextMoves = current.moves + 1
        val nextScore = (1000 - (nextMoves * 10)).coerceAtLeast(0)
        _uiState.value = current.copy(moves = nextMoves, score = nextScore)
        if (moved) {
            val nextScore2 = (1000 - (nextMoves * 10)).coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(score = nextScore2)
        }
    }

    fun onMemoryMatchCompleted(timeTakenSeconds: Int) {
        if (_uiState.value.mode != BrainGameMode.MEMORY_MATCH || _uiState.value.isCompleted) return
        val finalScore = (1000 - (_uiState.value.moves * 10) - (timeTakenSeconds * 3)).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            score = finalScore,
            elapsedSeconds = timeTakenSeconds,
            isPlaying = false,
            isCompleted = true
        )
        saveScore(finalScore, timeTakenSeconds)
    }

    fun onMathAnswer(isCorrect: Boolean) {
        if (_uiState.value.mode != BrainGameMode.MATH_SPRINT || !_uiState.value.isPlaying) return
        val current = _uiState.value
        val nextCorrect = if (isCorrect) current.correctAnswers + 1 else current.correctAnswers
        val nextWrong = if (isCorrect) current.wrongAnswers else current.wrongAnswers + 1
        val nextScore = (nextCorrect * 100) - (nextWrong * 25)
        _uiState.value = current.copy(
            correctAnswers = nextCorrect,
            wrongAnswers = nextWrong,
            score = nextScore.coerceAtLeast(0)
        )
    }

    fun onMathSprintFinished() {
        if (_uiState.value.mode != BrainGameMode.MATH_SPRINT || _uiState.value.isCompleted) return
        val current = _uiState.value
        val finalScore = (current.correctAnswers * 100) - (current.wrongAnswers * 25)
        _uiState.value = current.copy(
            score = finalScore.coerceAtLeast(0),
            isPlaying = false,
            isCompleted = true,
            elapsedSeconds = 30
        )
        saveScore(finalScore, 30)
    }

    fun loadBestScores(mode: BrainGameMode? = null) {
        viewModelScope.launch {
            val selectedMode = mode ?: _uiState.value.mode
            val result = repository.getBestScore(selectedMode)
            val best = result.getOrNull()?.score ?: 0
            lastSavedBestScore = best
            _uiState.value = _uiState.value.copy(bestScore = best)
        }
    }

    private fun startMathSprintTimer() {
        viewModelScope.launch {
            var remaining = 30
            while (remaining > 0 && _uiState.value.isPlaying && _uiState.value.mode == BrainGameMode.MATH_SPRINT) {
                delay(1000)
                remaining -= 1
                _uiState.value = _uiState.value.copy(timeRemainingSeconds = remaining, elapsedSeconds = 30 - remaining)
            }
            if (_uiState.value.mode == BrainGameMode.MATH_SPRINT && _uiState.value.isPlaying) {
                onMathSprintFinished()
            }
        }
    }

    private fun saveScore(finalScore: Int, durationSeconds: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingScore = true, errorMessage = null)
            val mode = _uiState.value.mode
            val scoreModel = BrainGameScoreModel(
                id = java.util.UUID.randomUUID().toString(),
                mode = mode.name,
                score = finalScore,
                durationSeconds = durationSeconds,
                moves = if (mode == BrainGameMode.MEMORY_MATCH) _uiState.value.moves else null,
                correctAnswers = if (mode == BrainGameMode.MATH_SPRINT) _uiState.value.correctAnswers else null,
                wrongAnswers = if (mode == BrainGameMode.MATH_SPRINT) _uiState.value.wrongAnswers else null,
                accuracy = if (mode == BrainGameMode.MATH_SPRINT) {
                    val total = _uiState.value.correctAnswers + _uiState.value.wrongAnswers
                    if (total > 0) _uiState.value.correctAnswers.toDouble() / total else 0.0
                } else null,
                playedAfterSession = playedAfterSession,
                relatedSessionId = sessionId
            )
            val result = repository.saveScore(scoreModel)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Could not save score. Please try again",
                    isSavingScore = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isSavingScore = false)
            }
            loadBestScores()
        }
    }
}
