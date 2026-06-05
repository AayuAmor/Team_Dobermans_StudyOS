package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.Task
import java.time.LocalDate

class TaskRepository {

    private val remoteTaskStore = mutableListOf(
        Task(
            title = "Revise DSA Notes",
            description = "Graphs, Trees and Sorting Algorithms",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            priority = Priority.HIGH,
            subjectId = "dsa",
            subjectName = "Data Structures"
        ),
        Task(
            title = "Complete Mock Test",
            description = "Practice exam simulation",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            priority = Priority.MEDIUM,
            subjectId = "mock",
            subjectName = "Mock Test"
        ),
        Task(
            title = "Focus Session",
            description = "Complete one Pomodoro cycle",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            priority = Priority.LOW,
            subjectId = "focus",
            subjectName = "Focus"
        )
    )

    fun getAllTasks(): List<Task> {
        return remoteTaskStore
    }

    fun getTodaysTasks(): List<Task> {
        val today = LocalDate.now()

        return remoteTaskStore.filter {
            it.startDate == today || it.endDate == today
        }
    }

    fun getHighPriorityTasks(): List<Task> {
        return remoteTaskStore.filter {
            it.priority == Priority.HIGH
        }
    }

    fun getTasksBySubject(subjectId: String): List<Task> {
        return remoteTaskStore.filter {
            it.subjectId == subjectId
        }
    }

    fun insertTask(task: Task) {
        remoteTaskStore.add(task)
    }

    fun updateTask(updatedTask: Task) {
        val index = remoteTaskStore.indexOfFirst {
            it.id == updatedTask.id
        }

        if (index != -1) {
            remoteTaskStore[index] = updatedTask
        }
    }

    fun deleteTask(taskId: String) {
        remoteTaskStore.removeAll {
            it.id == taskId
        }
    }

    fun getTodayTaskCount(): Int {
        return getTodaysTasks().size
    }

    fun getTotalTaskCount(): Int {
        return remoteTaskStore.size
    }
}