package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.data.analytics.AnalyticsRepository
import com.teamdobermans.studyos.model.FocusSessionModel
import java.util.UUID
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.FocusSessionRepoImpl
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FocusUiState(
    val activeSound: String? = null
)

class FocusViewModel : ViewModel() {

    private val repo     = FocusSessionRepoImpl()
    private val taskRepo = TaskRepository()
    private val auth     = FirebaseAuth.getInstance()
    private val streakRepo = AnalyticsRepository()

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _lastCompletedSessionId = MutableStateFlow<String?>(null)
    val lastCompletedSessionId: StateFlow<String?> = _lastCompletedSessionId.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepo.getActiveTasksFlow().collect { tasks ->
                _tasks.value = tasks
            }
        }
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
    }

    fun clearSelectedTask() {
        _selectedTask.value = null
    }

    fun completeSession(durationMinutes: Float): String? {
        val uid  = auth.currentUser?.uid ?: run {
            viewModelScope.launch { _toastMessage.emit("Please sign in to track your streak") }
            return null
        }
        val task = _selectedTask.value

        val sessionId = UUID.randomUUID().toString()
        val completedAt = System.currentTimeMillis()
        val startedAt = completedAt - (durationMinutes * 60_000L).toLong()

        viewModelScope.launch {
            val session = FocusSessionModel(
                id = sessionId,
                taskId = task?.id ?: "",
                taskTitle = task?.title ?: "No Task",
                durationMinutes = durationMinutes.toInt(),
                startedAt = startedAt,
                completedAt = completedAt,
                userId = uid
            )

            val saved = repo.saveSession(session)
            if (saved) {
                val existingStreak = streakRepo.getStreak().getOrNull() ?: com.teamdobermans.studyos.model.StudyStreakModel()
                val alreadyCountedToday = existingStreak.lastStudyDate == LocalDate.now().toString()
                val streakResult = streakRepo.recordStudyActivity("focus_session")
                val message = when {
                    streakResult.isFailure -> {
                        val error = streakResult.exceptionOrNull()?.message ?: "Unable to update streak"
                        if (error.contains("sign in", ignoreCase = true)) "Please sign in to track your streak" else error
                    }
                    alreadyCountedToday -> "Your streak is already counted for today"
                    else -> "Study streak updated"
                }
                _toastMessage.emit(message)
            }

            if (task != null) {
                taskRepo.linkSessionToTask(task.id, sessionId)
            }
        }

        _lastCompletedSessionId.value = sessionId
        return sessionId
    }

    fun toggleSound(sound: String) {
        val current = _state.value.activeSound
        _state.value = _state.value.copy(
            activeSound = if (current == sound) null else sound
        )
    }
}