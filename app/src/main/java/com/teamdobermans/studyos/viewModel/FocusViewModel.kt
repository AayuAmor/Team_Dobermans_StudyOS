package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.model.FocusSessionModel
import java.util.UUID
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.FocusSessionRepoImpl
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusUiState(
    val activeSound: String? = null
)

class FocusViewModel : ViewModel() {

    private val repo     = FocusSessionRepoImpl()
    private val taskRepo = TaskRepository()
    private val auth     = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

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

    fun completeSession(durationMinutes: Float) {
        val uid  = auth.currentUser?.uid ?: return
        val task = _selectedTask.value

        viewModelScope.launch {
            val sessionId   = UUID.randomUUID().toString()
            val completedAt = System.currentTimeMillis()
            val startedAt   = completedAt - (durationMinutes * 60_000L).toLong()

            val session = FocusSessionModel(
                id              = sessionId,
                taskId          = task?.id    ?: "",
                taskTitle       = task?.title ?: "No Task",
                durationMinutes = durationMinutes.toInt(),
                startedAt       = startedAt,
                completedAt     = completedAt,
                userId          = uid
            )

            repo.saveSession(session)

            if (task != null) {
                taskRepo.linkSessionToTask(task.id, sessionId)
            }
        }
    }

    fun toggleSound(sound: String) {
        val current = _state.value.activeSound
        _state.value = _state.value.copy(
            activeSound = if (current == sound) null else sound
        )
    }
}