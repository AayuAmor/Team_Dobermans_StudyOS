package com.teamdobermans.studyos.ui.plan
import com.teamdobermans.studyos.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.CalendarEvent
import com.teamdobermans.studyos.model.CalendarEventType
import com.teamdobermans.studyos.model.CalendarViewMode
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.SubjectModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.CalendarUiState
import com.teamdobermans.studyos.viewModel.CalendarViewModel
import com.teamdobermans.studyos.viewModel.PlanViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle

class PlanActivity : ComponentActivity() {
    private val viewModel: PlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlanBody(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBody(viewModel: PlanViewModel) {
    val calendarVm: CalendarViewModel = viewModel()
    val calendarState by calendarVm.uiState.collectAsState()
    val selectedDate = calendarState.selectedDate
    val viewMode = calendarState.viewMode

    val currentLocale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()

    var displayedMonth by remember { mutableStateOf(YearMonth.of(today.year, today.month)) }
    var displayedWeekStart by remember { mutableStateOf(today.with(DayOfWeek.MONDAY)) }

    val dayHeaders = listOf("M", "T", "W", "Th", "F", "S", "Su")

    val dateFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("MMM dd, yyyy", currentLocale) }
    val sectionFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("EEEE, MMM d", currentLocale) }
    val weekLabelFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("MMM d", currentLocale) }

    var priorityDropdown        by remember { mutableStateOf(false) }
    var filterDropdownExpanded  by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker          by remember { mutableStateOf(false) }

    val dynamicSubjects = viewModel.dynamicSubjects
    var selectedSubjectState by remember {
        mutableStateOf(dynamicSubjects.firstOrNull() ?: SubjectModel("sub_fallback", "General Study"))
    }

    val allNotes by viewModel.allNotes.collectAsState()

    val displayDueDateString = remember(viewModel.selectedStartDate, viewModel.selectedEndDate) {
        val startFormatted = viewModel.selectedStartDate.format(dateFormatter)
        if (viewModel.selectedEndDate != null) {
            "$startFormatted - ${viewModel.selectedEndDate!!.format(dateFormatter)}"
        } else {
            startFormatted
        }
    }

    val filteredTasks = if (viewModel.currentFilterSubjectId == "ALL_FILTER") {
        viewModel.tasks
    } else {
        viewModel.tasks.filter { it.subjectId == viewModel.currentFilterSubjectId }
    }

    val tasksForSelectedDate = filteredTasks.filter { task ->
        val end = task.endDate ?: task.startDate
        !selectedDate.isBefore(task.startDate) && !selectedDate.isAfter(end)
    }

    val sessionsForSelectedDate = calendarState.eventsForSelectedDate
        .filter { it.type == CalendarEventType.SESSION }

    val pendingCount = filteredTasks.count { !it.done }

    // Refresh sessions every time PlanBody enters composition (handles nav-back after completing a session)
    LaunchedEffect(Unit) {
        calendarVm.refreshSessions()
    }

    fun onDateTapped(date: LocalDate) {
        calendarVm.selectDate(date)
        viewModel.selectedStartDate = date
        viewModel.selectedEndDate = null
    }

    // Note picker dialog
    if (viewModel.showNotePicker) {
        val taskId = viewModel.notePickerTaskId ?: ""
        val alreadyLinked = viewModel.getLinkedNoteIds(taskId)
        val availableNotes = allNotes.filter { it.id !in alreadyLinked }

        AlertDialog(
            onDismissRequest = { viewModel.closeNotePicker() },
            title = {
                Text("Attach Note", fontWeight = FontWeight.Bold, color = StudyPurple)
            },
            text = {
                if (availableNotes.isEmpty()) {
                    Text("No notes available to attach.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableNotes.forEach { note ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.attachNote(taskId, note.id)
                                        viewModel.closeNotePicker()
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = StudyPurpleLight
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        note.title.ifBlank { "Untitled" },
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudyPurple,
                                        fontSize = 14.sp
                                    )
                                    if (note.body.isNotBlank()) {
                                        Text(note.body, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.closeNotePicker() }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Date range picker dialog
    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis   = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null) {
                            viewModel.selectedStartDate = Instant.ofEpochMilli(startMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.selectedEndDate   = if (endMillis != null) {
                                Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            } else null
                        }
                        showDatePicker = false
                    }
                ) { Text("Select", color = StudyPurple, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                title = {
                    Text("Select Due Range", modifier = Modifier.padding(start = 24.dp), fontWeight = FontWeight.Bold)
                },
                headline = {
                    Text("Choose Dates", modifier = Modifier.padding(start = 24.dp), fontSize = 14.sp)
                },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor         = StudyPurple,
                    dayInSelectionRangeContainerColor = StudyPurpleLight,
                    todayDateBorderColor              = StudyPurple
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep)
            .imePadding()
    ) {
        // ── Header ──────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    val selectedFilterLabel = if (viewModel.currentFilterSubjectId == "ALL_FILTER") "All"
                    else dynamicSubjects.find { it.id == viewModel.currentFilterSubjectId }?.name ?: "Unknown"
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { filterDropdownExpanded = true }
                    ) {
                        Text(
                            selectedFilterLabel,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = filterDropdownExpanded,
                        onDismissRequest = { filterDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                viewModel.currentFilterSubjectId = "ALL_FILTER"
                                filterDropdownExpanded = false
                            }
                        )
                        dynamicSubjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    viewModel.currentFilterSubjectId = subject.id
                                    filterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Plan",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
            if (pendingCount > 0) {
                Text(
                    "$pendingCount Task${if (pendingCount > 1) "s" else ""} Remaining",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }

        // ── Scroll body ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudyPurpleFaint)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ── Calendar card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

                    // Left: large date display
                    Column(
                        modifier = Modifier
                            .width(72.dp)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            selectedDate.month.getDisplayName(JavaTextStyle.SHORT, currentLocale),
                            color = StudyPurple,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, currentLocale),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "${selectedDate.dayOfMonth}",
                            color = StudyPurple,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp,
                            lineHeight = 48.sp
                        )
                    }

                    // Right: view controls + calendar grid
                    Column(modifier = Modifier.weight(1f)) {

                        // Controls: toggle + nav
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Month / Week toggle
                            Row(
                                modifier = Modifier
                                    .background(StudyPurpleLight, RoundedCornerShape(50.dp))
                                    .padding(2.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.clickable {
                                        calendarVm.setViewMode(CalendarViewMode.MONTH)
                                        displayedMonth = YearMonth.of(selectedDate.year, selectedDate.month)
                                    },
                                    color = if (viewMode == CalendarViewMode.MONTH) StudyPurple else Color.Transparent,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        "Month",
                                        fontSize = 11.sp,
                                        color = if (viewMode == CalendarViewMode.MONTH) Color.White else TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.clickable {
                                        calendarVm.setViewMode(CalendarViewMode.WEEK)
                                        displayedWeekStart = selectedDate.with(DayOfWeek.MONDAY)
                                    },
                                    color = if (viewMode == CalendarViewMode.WEEK) StudyPurple else Color.Transparent,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        "Week",
                                        fontSize = 11.sp,
                                        color = if (viewMode == CalendarViewMode.WEEK) Color.White else TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Prev / Label / Next
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (viewMode == CalendarViewMode.MONTH)
                                            displayedMonth = displayedMonth.minusMonths(1)
                                        else
                                            displayedWeekStart = displayedWeekStart.minusWeeks(1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                val navLabel = if (viewMode == CalendarViewMode.MONTH) {
                                    displayedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", currentLocale))
                                } else {
                                    val weekEnd = displayedWeekStart.plusDays(6)
                                    "${displayedWeekStart.format(weekLabelFormatter)} – ${weekEnd.format(weekLabelFormatter)}"
                                }
                                Text(
                                    navLabel,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(
                                    onClick = {
                                        if (viewMode == CalendarViewMode.MONTH)
                                            displayedMonth = displayedMonth.plusMonths(1)
                                        else
                                            displayedWeekStart = displayedWeekStart.plusWeeks(1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day headers row (shared between month and week views)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            dayHeaders.forEach { day ->
                                Text(
                                    day,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (viewMode == CalendarViewMode.MONTH) {
                            MonthGrid(
                                displayedMonth = displayedMonth,
                                selectedDate = selectedDate,
                                today = today,
                                datesWithEvents = calendarState.datesWithEvents,
                                onDateTapped = { date ->
                                    onDateTapped(date)
                                    if (YearMonth.from(date) != displayedMonth) {
                                        displayedMonth = YearMonth.from(date)
                                    }
                                }
                            )
                        } else {
                            WeekStrip(
                                weekStart = displayedWeekStart,
                                selectedDate = selectedDate,
                                today = today,
                                datesWithEvents = calendarState.datesWithEvents,
                                onDateTapped = { date ->
                                    onDateTapped(date)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Events section ───────────────────────────────────────────────────
            Text(
                selectedDate.format(sectionFormatter),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudyPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tasks for selected date
            tasksForSelectedDate.forEach { task ->
                TaskCard(
                    task = task,
                    isEditing = task.id == viewModel.editingTaskId,
                    allNotes = allNotes,
                    dateFormatter = dateFormatter,
                    onTaskClick = { viewModel.startEditing(task) },
                    onToggleDone = { viewModel.toggleTaskCompletion(task) },
                    onAttachNote = { viewModel.openNotePicker(task.id) },
                    onDetachNote = { noteId -> viewModel.detachNote(task.id, noteId) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Study Sessions section — always visible with its own empty state
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_history_24),
                    contentDescription = null,
                    tint = StudyPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Study Sessions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = StudyPurple
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (sessionsForSelectedDate.isEmpty()) {
                NoSessionsState()
            } else {
                sessionsForSelectedDate.forEach { event ->
                    SessionEventItem(event)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Task form card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isEditing = viewModel.editingTaskId != null

                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Editing Task Properties",
                                color = StudyPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Cancel",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { viewModel.cancelEditing() }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.taskTitle,
                        onValueChange = { viewModel.taskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("Task Title.....", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor   = Color.White,
                            unfocusedIndicatorColor = StudyPurpleLight,
                            focusedIndicatorColor   = StudyPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = viewModel.taskDescription,
                        onValueChange = { viewModel.taskDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Description / Category Details.....", color = Color.Gray) },
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor   = Color.White,
                            unfocusedIndicatorColor = StudyPurpleLight,
                            focusedIndicatorColor   = StudyPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = displayDueDateString,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1.1f).clickable { showDatePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            label = { Text("Due Date / Range", fontSize = 11.sp, color = StudyPurple) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_more_horiz_24),
                                    contentDescription = "Open Range Calendar Picker",
                                    tint = StudyPurple,
                                    modifier = Modifier.size(18.dp).clickable { showDatePicker = true }
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                disabledContainerColor   = StudyPurpleLight,
                                disabledTextColor        = TextPrimary,
                                disabledLabelColor       = StudyPurple,
                                disabledIndicatorColor   = Color.Transparent,
                                disabledTrailingIconColor = StudyPurple
                            )
                        )

                        Box(modifier = Modifier.weight(0.9f).align(Alignment.Bottom)) {
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = StudyPurpleLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable { subjectDropdownExpanded = true }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Linked Subject", fontSize = 10.sp, color = StudyPurple)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            selectedSubjectState.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_more_horiz_24),
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = subjectDropdownExpanded,
                                onDismissRequest = { subjectDropdownExpanded = false }
                            ) {
                                dynamicSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.name) },
                                        onClick = {
                                            selectedSubjectState = subject
                                            subjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(0.7f).align(Alignment.Bottom)) {
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = StudyPurpleLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable { priorityDropdown = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        viewModel.selectedPriority.name
                                            .lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        color = when (viewModel.selectedPriority) {
                                            Priority.HIGH   -> Color(0xFFE53935)
                                            Priority.MEDIUM -> Color(0xFFFFC107)
                                            Priority.LOW    -> Color(0xFF4CAF50)
                                        },
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_more_horiz_24),
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = priorityDropdown,
                                onDismissRequest = { priorityDropdown = false }
                            ) {
                                Priority.values().forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(p.name.lowercase().replaceFirstChar { it.uppercase() })
                                        },
                                        onClick = {
                                            viewModel.selectedPriority = p
                                            priorityDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick  = {
                            viewModel.handleAddOrUpdateTask(selectedSubjectState.id, selectedSubjectState.name)
                        },
                        enabled  = viewModel.taskTitle.trim().isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(24.dp)),
                        shape  = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                    ) {
                        Text(
                            if (viewModel.editingTaskId != null) "Update Details" else "+ Add Task",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MonthGrid(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    datesWithEvents: Set<LocalDate>,
    onDateTapped: (LocalDate) -> Unit
) {
    val firstDayOffset = displayedMonth.atDay(1).dayOfWeek.value - 1 // 0 = Monday
    val daysInMonth = displayedMonth.lengthOfMonth()
    val rows = (firstDayOffset + daysInMonth + 6) / 7

    for (row in 0 until rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (col in 0 until 7) {
                val dayNum = row * 7 + col - firstDayOffset + 1
                if (dayNum in 1..daysInMonth) {
                    val date = displayedMonth.atDay(dayNum)
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val isPast = date.isBefore(today)
                    val hasEvents = date in datesWithEvents

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .clickable { onDateTapped(date) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) StudyPurple else Color.Transparent)
                                .border(
                                    border = if (isToday && !isSelected)
                                        BorderStroke(1.5.dp, StudyPurple)
                                    else
                                        BorderStroke(0.dp, Color.Transparent),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 11.sp,
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    isPast     -> Color.LightGray
                                    isToday    -> StudyPurple
                                    else       -> TextPrimary
                                }
                            )
                        }
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) StudyPurple else StudyPurple.copy(alpha = 0.5f))
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).padding(2.dp))
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    datesWithEvents: Set<LocalDate>,
    onDateTapped: (LocalDate) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val isSelected = date == selectedDate
            val isToday = date == today
            val hasEvents = date in datesWithEvents

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp)
                    .clickable { onDateTapped(date) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (isSelected) StudyPurple else Color.Transparent)
                        .border(
                            border = if (isToday && !isSelected)
                                BorderStroke(1.5.dp, StudyPurple)
                            else
                                BorderStroke(0.dp, Color.Transparent),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${date.dayOfMonth}",
                        fontSize = 12.sp,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isSelected -> Color.White
                            isToday    -> StudyPurple
                            else       -> TextPrimary
                        }
                    )
                }
                if (hasEvents) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) StudyPurple else StudyPurple.copy(alpha = 0.5f))
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionEventItem(event: CalendarEvent) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val timeLabel = when {
        event.startTime != null && event.endTime != null ->
            "${event.startTime.format(timeFormatter)} – ${event.endTime.format(timeFormatter)}"
        event.endTime != null ->
            "Ended ${event.endTime.format(timeFormatter)}"
        event.startTime != null ->
            event.startTime.format(timeFormatter)
        else -> ""
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(12.dp),
        color  = Color.White,
        border = BorderStroke(1.dp, StudyPurpleLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudyPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_history_24),
                    contentDescription = null,
                    tint = StudyPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
                if (event.durationMinutes != null) {
                    Text(
                        "${event.durationMinutes} min session",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            if (timeLabel.isNotEmpty()) {
                Text(
                    timeLabel,
                    color = StudyPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NoSessionsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(12.dp),
        color  = Color.White,
        border = BorderStroke(1.dp, StudyPurpleLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No study sessions for this date",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    isEditing: Boolean,
    allNotes: List<NoteModel>,
    dateFormatter: DateTimeFormatter,
    onTaskClick: () -> Unit,
    onToggleDone: () -> Unit,
    onAttachNote: () -> Unit,
    onDetachNote: (String) -> Unit
) {
    val isOverdue = task.isOverdue()
    val linkedNotes = remember(task.linkedNoteIds, allNotes) {
        allNotes.filter { it.id in task.linkedNoteIds }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTaskClick() },
        shape  = RoundedCornerShape(14.dp),
        color  = if (isOverdue) Color(0xFFFFF2F1) else Color.White,
        border = if (isOverdue) BorderStroke(1.dp, Color(0xFFFFCDD2)) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.done) StudyPurple
                            else if (isOverdue) PriorityHighBg
                            else StudyPurpleLight
                        )
                        .clickable { onToggleDone() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.done) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_check_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            task.title,
                            color = if (task.done) Color.Gray else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = StudyPurple.copy(alpha = 0.1f)) {
                            Text(
                                task.subjectName,
                                color = StudyPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                    if (task.description.isNotEmpty()) {
                        Text(
                            task.description,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    val formattedDeadlineString = remember(task.startDate, task.endDate) {
                        val start = task.startDate.format(dateFormatter)
                        if (task.endDate != null) "$start - ${task.endDate.format(dateFormatter)}" else start
                    }
                    Text(
                        text = if (isOverdue) "Overdue: $formattedDeadlineString" else "Due: $formattedDeadlineString",
                        color = if (isOverdue) PriorityHigh else StudyPurple.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Light
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = when (task.priority) {
                        Priority.HIGH   -> Color(0xFFFFEBEB)
                        Priority.MEDIUM -> Color(0xFFFFF8E1)
                        Priority.LOW    -> Color(0xFFE8F5E9)
                    }
                ) {
                    Text(
                        task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = when (task.priority) {
                            Priority.HIGH   -> Color(0xFFE53935)
                            Priority.MEDIUM -> Color(0xFFF57F17)
                            Priority.LOW    -> Color(0xFF2E7D32)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (isEditing || linkedNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = StudyPurpleLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Linked Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudyPurple)
                    if (isEditing) {
                        Surface(
                            modifier = Modifier.clickable { onAttachNote() },
                            shape = RoundedCornerShape(8.dp),
                            color = StudyPurple
                        ) {
                            Text(
                                "+ Attach Note",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (linkedNotes.isEmpty()) {
                    Text("No notes attached", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                } else {
                    linkedNotes.forEach { note ->
                        LinkedNoteChip(note = note, showRemove = isEditing, onRemove = { onDetachNote(note.id) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedNoteChip(
    note: NoteModel,
    showRemove: Boolean,
    onRemove: () -> Unit
) {
    Surface(shape = RoundedCornerShape(8.dp), color = StudyPurpleLight) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text("•", color = StudyPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    note.title.ifBlank { "Untitled" },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            if (showRemove) {
                Icon(
                    painter = painterResource(R.drawable.ic_clear),
                    contentDescription = "Remove note",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp).clickable { onRemove() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanPreview() {
    StudyOSTheme { PlanBody(viewModel = PlanViewModel()) }
}
