package com.teamdobermans.studyos

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle

enum class Priority { HIGH, MEDIUM, LOW }

data class Task(
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Priority,
    val subjectId: String,
    val subjectName: String,
    var done: Boolean = false
)

class PlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PlanBody() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBody() {
    val context  = LocalContext.current
    val activity = context as? Activity
    val currentLocale = LocalConfiguration.current.locales[0]

    val today          = LocalDate.now()
    var selectedDate   by remember { mutableStateOf(today) }
    val currentMonth   = YearMonth.of(selectedDate.year, selectedDate.month)
    val dayHeaders     = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysInMonth    = currentMonth.lengthOfMonth()

    val dateFormatter  = remember(currentLocale) { DateTimeFormatter.ofPattern("MMM dd, yyyy", currentLocale) }

    var taskTitle        by remember { mutableStateOf("") }
    var taskDescription  by remember { mutableStateOf("") }
    var priorityDropdown by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    var filterDropdownExpanded by remember { mutableStateOf(false) }
    var currentFilterSubjectId by remember { mutableStateOf("ALL_FILTER") }
    val dynamicNoteSubjects = StudyDataRepository.dynamicSubjects

    var displayDueDate   by remember { mutableStateOf(today.format(dateFormatter)) }
    var showDatePicker   by remember { mutableStateOf(false) }

    val tasks            = remember {
        mutableStateListOf(
            Task("Read Chapter 5", "Focusing on cellular structures", "May 22, 2026", Priority.HIGH, "sub_gen", "General Study", done = true),
            Task("Project Work of English", "Draft introduction segment", "May 25, 2026 - May 28, 2026", Priority.MEDIUM, "sub_eng_comp", "Advanced Technical Writing")
        )
    }

    val filteredTasks = if (currentFilterSubjectId == "ALL_FILTER") tasks else tasks.filter { it.subjectId == currentFilterSubjectId }
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
                            val startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            displayDueDate = if (endMillis != null) {
                                val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                                "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}"
                            } else {
                                startDate.format(dateFormatter)
                            }
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
                title = { Text(text = "Select Due Range", modifier = Modifier.padding(start = 24.dp), fontWeight = FontWeight.Bold) },
                headline = { Text(text = "Choose Dates", modifier = Modifier.padding(start = 24.dp), fontSize = 14.sp) },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = StudyPurple,
                    dayInSelectionRangeContainerColor = StudyPurple.copy(alpha = 0.15f),
                    todayDateBorderColor = StudyPurple
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
                .background(StudyPurple)
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
                        val selectedFilterLabel = if (currentFilterSubjectId == "ALL_FILTER") "All" else dynamicNoteSubjects.find { it.id == currentFilterSubjectId }?.name ?: "Unknown"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { filterDropdownExpanded = true }
                        ) {
                            Text(selectedFilterLabel, color = Color.White, fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        DropdownMenu(expanded = filterDropdownExpanded, onDismissRequest = { filterDropdownExpanded = false }) {
                            DropdownMenuItem(text = { Text("All") }, onClick = { currentFilterSubjectId = "ALL_FILTER"; filterDropdownExpanded = false })
                            dynamicNoteSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        currentFilterSubjectId = subject.id
                                        filterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Plan", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
                if (pendingCount > 0)
                    Text("$pendingCount Task${if (pendingCount > 1) "s" else ""} Remaining",
                        color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
                    .background(StudyPurpleLight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.width(80.dp)) {
                            Text(selectedDate.month.getDisplayName(JavaTextStyle.SHORT, currentLocale),
                                color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, currentLocale),
                                color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${selectedDate.dayOfMonth}", color = StudyPurple,
                                fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                dayHeaders.forEach { day ->
                                    Text(day, fontSize = 11.sp, color = Color.Gray,
                                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val rows = ((firstDayOfWeek - 1 + daysInMonth) + 6) / 7
                            for (row in 0 until rows) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    for (col in 0 until 7) {
                                        val dayNum = row * 7 + col - (firstDayOfWeek - 1) + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val date       = currentMonth.atDay(dayNum)
                                            val isSelected = date == selectedDate
                                            val isToday    = date == today
                                            val isPast     = date.isBefore(today)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(2.dp)
                                                    .aspectRatio(1f)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) StudyPurple else Color.Transparent)
                                                    .border(
                                                        border = if (isToday && !isSelected) BorderStroke(1.5.dp, StudyPurple) else BorderStroke(0.dp, Color.Transparent),
                                                        shape = CircleShape
                                                    )
                                                    .clickable(enabled = !isPast) {
                                                        selectedDate = date
                                                        displayDueDate = date.format(dateFormatter)
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
                                                        isToday -> StudyPurple
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

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            placeholder = { Text("Task Title.....", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF0EEFF),
                                focusedContainerColor   = Color(0xFFF0EEFF),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor   = StudyPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = taskDescription,
                            onValueChange = { taskDescription = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Description / Category Details.....", color = Color.Gray) },
                            maxLines = 3,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF0EEFF),
                                focusedContainerColor   = Color(0xFFF0EEFF),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor   = StudyPurple
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = displayDueDate,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clickable { showDatePicker = true },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                label = { Text("Due Date / Range", fontSize = 11.sp, color = StudyPurple) },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_more_horiz_24),
                                        contentDescription = "Open Custom Range Picker Calendar",
                                        tint = StudyPurple,
                                        modifier = Modifier.size(18.dp).clickable { showDatePicker = true }
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    disabledContainerColor  = Color(0xFFF0EEFF),
                                    disabledTextColor       = Color(0xFF1A1A2E),
                                    disabledLabelColor      = StudyPurple,
                                    disabledIndicatorColor  = Color.Transparent,
                                    disabledTrailingIconColor = StudyPurple
                                )
                            )
                            Box(modifier = Modifier.weight(0.7f).align(Alignment.Bottom)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0EEFF),
                                    modifier = Modifier.fillMaxWidth().height(52.dp).clickable { priorityDropdown = true }) {
                                    Row(modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            selectedPriority.name.lowercase().replaceFirstChar { it.uppercase() },
                                            color = when (selectedPriority) {
                                                Priority.HIGH   -> Color(0xFFE53935)
                                                Priority.MEDIUM -> Color(0xFFFFC107)
                                                Priority.LOW    -> Color(0xFF4CAF50)
                                            },
                                            fontWeight = FontWeight.Medium, fontSize = 14.sp
                                        )
                                        Icon(painter = painterResource(R.drawable.baseline_more_horiz_24),
                                            contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = priorityDropdown, onDismissRequest = { priorityDropdown = false }) {
                                    Priority.values().forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                            onClick = { selectedPriority = p; priorityDropdown = false }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val trimmedTitle = taskTitle.trim()
                                if (trimmedTitle.isNotEmpty()) {
                                    tasks.add(Task(trimmedTitle, taskDescription.trim(), displayDueDate, selectedPriority, "sub_gen", "General Study"))
                                    taskTitle = ""
                                    taskDescription = ""
                                    displayDueDate = today.format(dateFormatter)
                                }
                            },
                            enabled = taskTitle.trim().isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                        ) {
                            Text("+ Add Task", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                filteredTasks.forEach { task ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(22.dp).clip(CircleShape)
                                    .background(if (task.done) StudyPurple else Color(0xFFEEEBFF))
                                    .clickable {
                                        val index = tasks.indexOf(task)
                                        if (index >= 0) tasks[index] = task.copy(done = !task.done)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.done) Icon(
                                    painter = painterResource(R.drawable.baseline_check_24),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    color = if (task.done) Color.Gray else Color(0xFF1A1A2E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                                )
                                if (task.description.isNotEmpty()) {
                                    Text(
                                        task.description,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                                Text(
                                    "Due: ${task.dueDate}",
                                    color = StudyPurple.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
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
                                        Priority.MEDIUM -> Color(0xFFFFC107)
                                        Priority.LOW    -> Color(0xFF4CAF50)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("×", color = Color.Gray, fontSize = 16.sp,
                                modifier = Modifier.clickable { tasks.remove(task) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanPreview() {
    StudyOSTheme { PlanBody() }
}