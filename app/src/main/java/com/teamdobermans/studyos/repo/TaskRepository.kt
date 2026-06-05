package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.Task
import java.time.LocalDate

class TaskRepository {
    private val remoteTaskStore = mutableListOf<Task>()

    fun getAllTasks(): List<Task> = remoteTaskStore

    fun insertTask(task: Task) {
        remoteTaskStore.add(task)
    }

    fun updateTask(updatedTask: Task) {
        val index = remoteTaskStore.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) remoteTaskStore[index] = updatedTask
    }
}
