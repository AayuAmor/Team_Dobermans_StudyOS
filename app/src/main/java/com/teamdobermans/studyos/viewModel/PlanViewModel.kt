package com.teamdobermans.studyos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.SubjectModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.repo.NoteRepoImpl
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class PlanViewModel : ViewModel() {
    private val repository = TaskRepository
    private val noteRepo = NoteRepoImpl()

    val allNotes: StateFlow<List<NoteModel>> = noteRepo.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dynamicSubjects = mutableStateListOf(
        SubjectModel("sub_gen",     "General Study"),
        SubjectModel("sub_cs_101",  "Computer Architecture"),
        SubjectModel("sub_math_3",  "Linear Algebra"),
        SubjectModel("sub_engcomp", "Advanced Technical Writing")
    )

    val tasks = mutableStateListOf<Task>()

    var taskTitle by mutableStateOf("")
    var taskDescription by mutableStateOf("")
    var selectedPriority by mutableStateOf(Priority.MEDIUM)
    var currentFilterSubjectId by mutableStateOf("ALL_FILTER")

    var selectedStartDate by mutableStateOf<LocalDate>(LocalDate.now())
    var selectedEndDate by mutableStateOf<LocalDate?>(null)
    var editingTaskId by mutableStateOf<String?>(null)
        private set

    // ── Note picker state ────────────────────────────────────────────────────
    var showNotePicker by mutableStateOf(false)
        private set
    var notePickerTaskId by mutableStateOf<String?>(null)
        private set

    init {
        syncFromRepository()
    }

    private fun syncFromRepository() {
        tasks.clear()
        tasks.addAll(repository.getAllTasks())
    }

    fun handleAddOrUpdateTask(subjectId: String, subjectName: String) {
        if (taskTitle.trim().isEmpty()) return

        val currentId = editingTaskId
        if (currentId != null) {
            val existingLinkedNoteIds = tasks.find { it.id == currentId }?.linkedNoteIds ?: emptyList()
            val taskToUpdate = Task(
                id = currentId,
                title = taskTitle.trim(),
                description = taskDescription.trim(),
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                priority = selectedPriority,
                subjectId = subjectId,
                subjectName = subjectName,
                linkedNoteIds = existingLinkedNoteIds
            )
            repository.updateTask(taskToUpdate)
            editingTaskId = null
        } else {
            val newTask = Task(
                title = taskTitle.trim(),
                description = taskDescription.trim(),
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                priority = selectedPriority,
                subjectId = subjectId,
                subjectName = subjectName
            )
            repository.insertTask(newTask)
        }
        syncFromRepository()
        resetFormInputs()
    }

    fun startEditing(task: Task) {
        editingTaskId = task.id
        taskTitle = task.title
        taskDescription = task.description
        selectedPriority = task.priority
        selectedStartDate = task.startDate
        selectedEndDate = task.endDate
    }

    fun toggleTaskCompletion(task: Task) {
        val updated = task.copy(done = !task.done)
        repository.updateTask(updated)
        syncFromRepository()
    }

    fun cancelEditing() {
        editingTaskId = null
        resetFormInputs()
    }

    // ── Note ↔ Task relationship ─────────────────────────────────────────────

    fun openNotePicker(taskId: String) {
        notePickerTaskId = taskId
        showNotePicker = true
    }

    fun closeNotePicker() {
        showNotePicker = false
        notePickerTaskId = null
    }

    fun attachNote(taskId: String, noteId: String) {
        repository.attachNoteToTask(taskId, noteId)
        syncFromRepository()
    }

    fun detachNote(taskId: String, noteId: String) {
        repository.removeNoteFromTask(taskId, noteId)
        syncFromRepository()
    }

    fun getLinkedNoteIds(taskId: String): List<String> =
        tasks.find { it.id == taskId }?.linkedNoteIds ?: emptyList()

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun resetFormInputs() {
        taskTitle = ""
        taskDescription = ""
        selectedPriority = Priority.MEDIUM
        selectedStartDate = LocalDate.now()
        selectedEndDate = null
    }
}