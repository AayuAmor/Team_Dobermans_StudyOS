package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.model.CalendarEvent
import com.teamdobermans.studyos.model.CalendarEventType
import com.teamdobermans.studyos.model.CalendarViewMode
import com.teamdobermans.studyos.repo.FocusSessionRepoImpl
import com.teamdobermans.studyos.repo.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val eventsForSelectedDate: List<CalendarEvent> = emptyList(),
    val datesWithEvents: Set<LocalDate> = emptySet()
)

class CalendarViewModel : ViewModel() {

    private val taskRepository = TaskRepository()
    private val sessionRepo = FocusSessionRepoImpl()
    private val auth = FirebaseAuth.getInstance()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    private val _sessionEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())

    val uiState: StateFlow<CalendarUiState> = combine(
        taskRepository.getActiveTasksFlow(),
        _sessionEvents,
        _selectedDate,
        _viewMode
    ) { tasks, sessionEvents, selected, mode ->
        val taskEvents = tasks.flatMap { task ->
            val end = task.endDate ?: task.startDate
            generateSequence(task.startDate) { d ->
                d.plusDays(1).takeIf { !it.isAfter(end) }
            }.take(366).map { date ->
                CalendarEvent(
                    id = "${task.id}_$date",
                    title = task.title,
                    date = date,
                    type = CalendarEventType.TASK
                )
            }
        }

        val allEvents = taskEvents + sessionEvents
        CalendarUiState(
            selectedDate = selected,
            viewMode = mode,
            eventsForSelectedDate = allEvents.filter { it.date == selected },
            datesWithEvents = allEvents.map { it.date }.toSet()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    init {
        loadSessions()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    private fun loadSessions() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val sessions = sessionRepo.getSessions(uid)
            _sessionEvents.value = sessions.mapNotNull { session ->
                if (session.completedAt == 0L) return@mapNotNull null
                val instant = Instant.ofEpochMilli(session.completedAt)
                val zone = ZoneId.systemDefault()
                CalendarEvent(
                    id = session.id,
                    title = session.taskTitle.ifBlank { "Focus Session" },
                    date = instant.atZone(zone).toLocalDate(),
                    type = CalendarEventType.SESSION,
                    durationMinutes = session.durationMinutes,
                    startTime = instant.atZone(zone).toLocalTime()
                )
            }
        }
    }
}