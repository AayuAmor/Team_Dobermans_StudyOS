package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class TaskRepository {

    private val store = mutableMapOf<String, Task>()
    private val _flow = MutableStateFlow<List<Task>>(emptyList())

    fun getAllTasks(): List<Task> = store.values.toList()

    fun insertTask(task: Task) {
        store[task.id] = task
        _flow.value = store.values.toList()
    }

    fun updateTask(updatedTask: Task) {
        store[updatedTask.id] = updatedTask
        _flow.value = store.values.toList()
    }

    fun getTodaysTasks(): List<Task> {
        val today = LocalDate.now()
        return store.values.filter { !it.done && !it.startDate.isAfter(today) }
    }

    fun getActiveTasksFlow(): Flow<List<Task>> = _flow

    fun linkSessionToTask(taskId: String, sessionId: String) {}

    fun getTasksForNote(noteId: String): List<Task> =
        store.values.filter { noteId in it.linkedNoteIds }

    fun attachNoteToTask(taskId: String, noteId: String) {
        val task = store[taskId] ?: return
        if (noteId !in task.linkedNoteIds) {
            store[taskId] = task.copy(linkedNoteIds = task.linkedNoteIds + noteId)
            _flow.value = store.values.toList()
        }
    }

    fun removeNoteFromTask(taskId: String, noteId: String) {
        val task = store[taskId] ?: return
        store[taskId] = task.copy(linkedNoteIds = task.linkedNoteIds.filter { it != noteId })
        _flow.value = store.values.toList()
    }
}