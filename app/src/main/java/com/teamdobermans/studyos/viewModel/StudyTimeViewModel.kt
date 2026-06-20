package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.StudySession
import com.teamdobermans.studyos.model.StudyTimeState
import com.teamdobermans.studyos.model.Subject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class StudyTimeViewModel : ViewModel() {

    // Default subjects loaded immediately
    private val defaultSubjects = listOf(
        Subject(name = "Mathematics"),
        Subject(name = "Physics"),
        Subject(name = "Chemistry"),
        Subject(name = "Literature"),
        Subject(name = "Computer Science")
    )

    private val _uiState = MutableStateFlow(
        StudyTimeState(subjects = defaultSubjects)
    )
    val uiState: StateFlow<StudyTimeState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun selectSubject(subject: Subject) {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun onNewSubjectNameChange(name: String) {
        _uiState.update { it.copy(newSubjectName = name) }
    }

    fun showAddSubjectDialog(show: Boolean) {
        _uiState.update { it.copy(showAddSubjectDialog = show, newSubjectName = "") }
    }

    fun addSubject() {
        val name = _uiState.value.newSubjectName.trim()
        if (name.isBlank()) return
        val newSubject = Subject(name = name)
        _uiState.update {
            it.copy(
                subjects = it.subjects + newSubject,
                showAddSubjectDialog = false,
                newSubjectName = "",
                selectedSubject = newSubject
            )
        }
    }

    fun deleteSubject(subjectId: String) {
        _uiState.update { state ->
            state.copy(
                subjects = state.subjects.filter { it.id != subjectId },
                selectedSubject = if (state.selectedSubject?.id == subjectId) null
                else state.selectedSubject
            )
        }
    }

    fun startTimer() {
        val subject = _uiState.value.selectedSubject ?: return
        val session = StudySession(
            id = UUID.randomUUID().toString(),
            subjectId = subject.id,
            subjectName = subject.name,
            startTimeMillis = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(
                isRunning = true,
                activeSession = session,
                elapsedMillis = 0L,
                sessions = it.sessions + session
            )
        }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _uiState.update { it.copy(elapsedMillis = it.elapsedMillis + 1_000L) }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        val state = _uiState.value
        val session = state.activeSession ?: return
        val now = System.currentTimeMillis()
        val completed = session.copy(
            endTimeMillis = now,
            durationMillis = state.elapsedMillis
        )
        _uiState.update {
            it.copy(
                isRunning = false,
                activeSession = null,
                elapsedMillis = 0L,
                sessions = it.sessions.map { s -> if (s.id == session.id) completed else s }
            )
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.update { it.copy(sessions = it.sessions.filter { s -> s.id != sessionId }) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}