package com.teamdobermans.studyos

import android.app.Activity
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
import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.viewModel.PlanViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
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
    val context = LocalContext.current
    val activity = context as? Activity
    val currentLocale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()

    var selectedDateCalendarView by remember { mutableStateOf(today) }
    val currentMonth = YearMonth.of(selectedDateCalendarView.year, selectedDateCalendarView.month)
    val dayHeaders = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysInMonth = currentMonth.lengthOfMonth()

    val dateFormatter = remember(currentLocale) { DateTimeFormatter.ofPattern("MMM dd, yyyy", currentLocale) }

    var priorityDropdown by remember { mutableStateOf(false) }
    var filterDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dynamicNoteSubjects = StudyDataRepository.dynamicSubjects
    var selectedSubjectState by remember {
        mutableStateOf(dynamicNoteSubjects.firstOrNull() ?: NoteSubject("sub_fallback", "General Study"))
    }

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
    val pendingCount = filteredTasks.count { !it.done }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null) {
                            viewModel.selectedStartDate = Instant.ofEpochMilli(startMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.selectedEndDate = if (endMillis != null) {
                                Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            } else {
                                null
                            }
                        }
                        showDatePicker = false
                    }
                ) { Text("Select", color = Color(0xFF6200EE), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                title = { Text(text = "Select Due Range", modifier = Modifier.padding(start = 24.dp), fontWeight = FontWeight.Bold) },
                headline = { Text(text = "Choose Dates", modifier = Modifier.padding(start = 24.dp), fontSize = 14.sp) },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF6200EE),
                    dayInSelectionRangeContainerColor = Color(0xFF6200EE).copy(alpha = 0.15f),
                    todayDateBorderColor = Color(0xFF6200EE)
                )
            )
        }
    }

    Scaffold(
        bottomBar = { StudyOSBottomNav(currentRoute = NavRoute.PLAN, context = context) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF6200EE))
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
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
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.clickable { activity?.finish() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Box {
                        val selectedFilterLabel = if (viewModel.currentFilterSubjectId == "ALL_FILTER") "All" else dynamicNoteSubjects.find { it.id == viewModel.currentFilterSubjectId }?.name ?: "Unknown"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { filterDropdownExpanded = true }
                        ) {
                            Text(selectedFilterLabel, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        DropdownMenu(expanded = filterDropdownExpanded, onDismissRequest = { filterDropdownExpanded = false }) {
                            DropdownMenuItem(text = { Text("All") }, onClick = { viewModel.currentFilterSubjectId = "ALL_FILTER"; filterDropdownExpanded = false })
                            dynamicNoteSubjects.forEach { subject ->
                                DropdownMenuItem(text = { Text(subject.name) }, onClick = { viewModel.currentFilterSubjectId = subject.id; filterDropdownExpanded = false })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Plan", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
                if (pendingCount > 0)
                    Text("$pendingCount Task${if (pendingCount > 1) "s" else ""} Remaining", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.width(80.dp)) {
                            Text(selectedDateCalendarView.month.getDisplayName(JavaTextStyle.SHORT, currentLocale), color = Color(0xFF6200EE), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(selectedDateCalendarView.dayOfWeek.getDisplayName(JavaTextStyle.FULL, currentLocale), color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${selectedDateCalendarView.dayOfMonth}", color = Color(0xFF6200EE), fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                dayHeaders.forEach { day ->
                                    Text(day, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val rows = ((firstDayOfWeek - 1 + daysInMonth) + 6) / 7
                            for (row in 0 until rows) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    for (col in 0 until 7) {
                                        val dayNum = row * 7 + col - (firstDayOfWeek - 1) + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val date = currentMonth.atDay(dayNum)
                                            val isSelected = date == selectedDateCalendarView
                                            val isToday = date == today
                                            val isPast = date.isBefore(today)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(2.dp)
                                                    .aspectRatio(1f)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color(0xFF6200EE) else Color.Transparent)
                                                    .border(
                                                        border = if (isToday && !isSelected) BorderStroke(1.5.dp, Color(0xFF6200EE)) else BorderStroke(0.dp, Color.Transparent),
                                                        shape = CircleShape
                                                    )
                                                    .clickable(enabled = !isPast) {
                                                        selectedDateCalendarView = date
                                                        viewModel.selectedStartDate = date
                                                        viewModel.selectedEndDate = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isPast -> Color.LightGray
                                                        isToday -> Color(0xFF6200EE)
                                                        else -> Color(0xFF1A1A2E)
                                                    }
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.weight(1f).padding(2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val isEditing = viewModel.editingTaskId != null

                        if (isEditing) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Editing Task Properties", color = Color(0xFF6200EE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Cancel", color = Color.Red, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.cancelEditing() })
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
                                unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF),
                                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color(0xFF6200EE)
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
                                unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF),
                                unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color(0xFF6200EE)
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
                                label = { Text("Due Date / Range", fontSize = 11.sp, color = Color(0xFF6200EE)) },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_more_horiz_24),
                                        contentDescription = "Open Range Calendar Picker",
                                        tint = Color(0xFF6200EE),
                                        modifier = Modifier.size(18.dp).clickable { showDatePicker = true }
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    disabledContainerColor = Color(0xFFF0EEFF), disabledTextColor = Color(0xFF1A1A2E),
                                    disabledLabelColor = Color(0xFF6200EE), disabledIndicatorColor = Color.Transparent, disabledTrailingIconColor = Color(0xFF6200EE)
                                )
                            )

                            Box(modifier = Modifier.weight(0.9f).align(Alignment.Bottom)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0EEFF),
                                    modifier = Modifier.fillMaxWidth().height(52.dp).clickable { subjectDropdownExpanded = true }) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.Center) {
                                        Text("Linked Subject", fontSize = 10.sp, color = Color(0xFF6200EE))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(selectedSubjectState.name, color = Color(0xFF1A1A2E), fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1)
                                            Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                DropdownMenu(expanded = subjectDropdownExpanded, onDismissRequest = { subjectDropdownExpanded = false }) {
                                    dynamicNoteSubjects.forEach { subject ->
                                        DropdownMenuItem(text = { Text(subject.name) }, onClick = { selectedSubjectState = subject; subjectDropdownExpanded = false })
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(0.7f).align(Alignment.Bottom)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0EEFF),
                                    modifier = Modifier.fillMaxWidth().height(52.dp).clickable { priorityDropdown = true }) {
                                    Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            viewModel.selectedPriority.name.lowercase().replaceFirstChar { it.uppercase() },
                                            color = when (viewModel.selectedPriority) {
                                                Priority.HIGH -> Color(0xFFE53935)
                                                Priority.MEDIUM -> Color(0xFFFFC107)
                                                Priority.LOW -> Color(0xFF4CAF50)
                                            },
                                            fontWeight = FontWeight.Medium, fontSize = 14.sp
                                        )
                                        Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = priorityDropdown, onDismissRequest = { priorityDropdown = false }) {
                                    Priority.values().forEach { p ->
                                        DropdownMenuItem(text = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { viewModel.selectedPriority = p; priorityDropdown = false })
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.handleAddOrUpdateTask(selectedSubjectState.id, selectedSubjectState.name) },
                            enabled = viewModel.taskTitle.trim().isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text(if (isEditing) "💾 Update Details" else "+ Add Task", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                filteredTasks.forEach { task ->
                    val isOverdue = task.isOverdue()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.startEditing(task) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isOverdue) Color(0xFFFFF2F1) else Color.White,
                        border = if (isOverdue) BorderStroke(1.dp, Color(0xFFFFCDD2)) else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(22.dp).clip(CircleShape)
                                    .background(if (task.done) Color(0xFF6200EE) else if (isOverdue) Color(0xFFFFEBEE) else Color(0xFFEEEBFF))
                                    .clickable { viewModel.toggleTaskCompletion(task) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.done) Icon(painter = painterResource(R.drawable.baseline_check_24), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        task.title,
                                        color = if (task.done) Color.Gray else Color(0xFF1A1A2E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF6200EE).copy(alpha = 0.1f)) {
                                        Text(task.subjectName, color = Color(0xFF6200EE), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), maxLines = 1)
                                    }
                                }
                                if (task.description.isNotEmpty()) {
                                    Text(task.description, color = Color.Gray, fontSize = 12.sp, textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None)
                                }

                                val formattedDeadLineString = remember(task.startDate, task.endDate) {
                                    val start = task.startDate.format(dateFormatter)
                                    if (task.endDate != null) "$start - ${task.endDate.format(dateFormatter)}" else start
                                }

                                Text(
                                    text = if (isOverdue) "⚠️ Overdue: $formattedDeadLineString" else "Due: $formattedDeadLineString",
                                    color = if (isOverdue) Color(0xFFD32F2F) else Color(0xFF6200EE).copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Light
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = when (task.priority) {
                                    Priority.HIGH -> Color(0xFFFFEBEB)
                                    Priority.MEDIUM -> Color(0xFFFFF8E1)
                                    Priority.LOW -> Color(0xFFE8F5E9)
                                }
                            ) {
                                Text(
                                    task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = when (task.priority) {
                                        Priority.HIGH -> Color(0xFFE53935)
                                        Priority.MEDIUM -> Color(0xFFF57F17)
                                        Priority.LOW -> Color(0xFF2E7D32)
                                    },
                                    fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanPreview() {
    StudyOSTheme { PlanBody(viewModel = PlanViewModel()) }
}