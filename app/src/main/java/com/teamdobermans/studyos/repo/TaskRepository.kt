package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class TaskRepository {

    private val store = mutableListOf<Task>()
    private val _flow = MutableStateFlow<List<Task>>(emptyList())

    fun getAllTasks(): List<Task> = store.toList()

    fun insertTask(task: Task) {
        store.add(task)
        _flow.value = store.toList()
    }

    fun updateTask(updatedTask: Task) {
        val i = store.indexOfFirst { it.id == updatedTask.id }
        if (i != -1) store[i] = updatedTask
        _flow.value = store.toList()
    }

    fun getTodaysTasks(): List<Task> {
        val today = LocalDate.now()
        return store.filter { !it.done && !it.startDate.isAfter(today) }
    }

    fun getActiveTasksFlow(): Flow<List<Task>> = _flow

    fun linkSessionToTask(taskId: String, sessionId: String) {
    }

    fun getTasksForNote(noteId: String): List<Task> =
        store.filter { noteId in it.linkedNoteIds }

    fun attachNoteToTask(taskId: String, noteId: String) {
        val i = store.indexOfFirst { it.id == taskId }
        if (i != -1 && noteId !in store[i].linkedNoteIds) {
            store[i] = store[i].copy(linkedNoteIds = store[i].linkedNoteIds + noteId)
            _flow.value = store.toList()
        }
    }

    fun removeNoteFromTask(taskId: String, noteId: String) {
        val i = store.indexOfFirst { it.id == taskId }
        if (i != -1) {
            store[i] = store[i].copy(linkedNoteIds = store[i].linkedNoteIds - noteId)
            _flow.value = store.toList()
        }
    }
}