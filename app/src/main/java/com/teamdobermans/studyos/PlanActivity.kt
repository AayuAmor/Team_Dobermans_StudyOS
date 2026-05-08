package com.teamdobermans.studyos

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

enum class Priority { HIGH, MEDIUM, LOW }

data class Task(val title: String, val dueDate: String, val priority: Priority, val subject: String, var done: Boolean = false)

class PlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PlanBody() }
    }
}

@Composable
fun PlanBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    val today        = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    val currentMonth = YearMonth.of(selectedDate.year, selectedDate.month)
    val dayHeaders   = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysInMonth  = currentMonth.lengthOfMonth()

    var taskTitle        by remember { mutableStateOf("") }
    var taskDate         by remember { mutableStateOf("") }
    var priorityDropdown by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var subjectDropdown  by remember { mutableStateOf(false) }
    val subjects         = remember { mutableStateListOf("General", "Biology", "Physics", "Math") }
    var selectedSubject  by remember { mutableStateOf("General") }
    val tasks            = remember { mutableStateListOf(
        Task("Read Chapter 5", "Apr 22", Priority.HIGH, "Biology", done = true),
        Task("Project Work of English", "Apr 25", Priority.MEDIUM, "English")
    )}

    val pendingCount = tasks.count { !it.done }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { activity?.finish() }) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = Color.White, fontSize = 14.sp)
                    }
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Text("All", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Plan", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            if (pendingCount > 0) Text("$pendingCount Task${if (pendingCount > 1) "s" else ""} Remaining", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()
        ) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.width(80.dp)) {
                        Text(selectedDate.month.getDisplayName(JavaTextStyle.SHORT, LocalLocale.current.platformLocale), color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, LocalLocale.current.platformLocale), color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${selectedDate.dayOfMonth}", color = StudyPurple, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            dayHeaders.forEach { day -> Text(day, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalCells = firstDayOfWeek - 1 + daysInMonth
                        val rows = (totalCells + 6) / 7
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                for (col in 0 until 7) {
                                    val dayNum = row * 7 + col - (firstDayOfWeek - 1) + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val date = currentMonth.atDay(dayNum)
                                        val isSelected = date == selectedDate
                                        val isToday    = date == today
                                        Box(modifier = Modifier.weight(1f).padding(2.dp).aspectRatio(1f).clip(CircleShape)
                                            .background(if (isSelected) StudyPurple else Color.Transparent).clickable { selectedDate = date },
                                            contentAlignment = Alignment.Center) {
                                            Text("$dayNum", fontSize = 11.sp, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when { isSelected -> Color.White; isToday -> StudyPurple; else -> Color(0xFF1A1A2E) })
                                        }
                                    } else { Box(modifier = Modifier.weight(1f).padding(2.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(value = taskTitle, onValueChange = { taskTitle = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        placeholder = { Text("New Task.....", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF), unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = taskDate, onValueChange = { taskDate = it }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp), singleLine = true,
                            placeholder = { Text("dd.......yy", color = Color.Gray, fontSize = 13.sp) },
                            trailingIcon = { Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                            colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF), unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple))
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0EEFF), modifier = Modifier.fillMaxWidth().height(52.dp).clickable { priorityDropdown = true }) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(selectedPriority.name.lowercase().replaceFirstChar { it.uppercase() }, color = when (selectedPriority) { Priority.HIGH -> Color(0xFFE53935); Priority.MEDIUM -> Color(0xFFFFC107); Priority.LOW -> Color(0xFF4CAF50) }, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(expanded = priorityDropdown, onDismissRequest = { priorityDropdown = false }) {
                                Priority.values().forEach { p -> DropdownMenuItem(text = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { selectedPriority = p; priorityDropdown = false }) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF0EEFF), modifier = Modifier.fillMaxWidth().height(52.dp).clickable { subjectDropdown = true }) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(selectedSubject, color = Color(0xFF1A1A2E), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                            subjects.forEach { subject -> DropdownMenuItem(text = { Text(subject) }, onClick = { selectedSubject = subject; subjectDropdown = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val trimmed = taskTitle.trim()
                            if (trimmed.isNotEmpty()) {
                                tasks.add(Task(trimmed, taskDate, selectedPriority, selectedSubject))
                                taskTitle = ""
                                taskDate  = ""
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

            tasks.forEach { task ->
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
                                .background(if (task.done) StudyPurple else Color.Transparent)
                                .clickable { val index = tasks.indexOf(task); if (index >= 0) tasks[index] = task.copy(done = !task.done) }
                                .then(if (!task.done) Modifier.background(Color.White).clip(CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.done) Icon(painter = painterResource(R.drawable.baseline_check_24), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            else Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFEEEBFF)))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            task.title,
                            modifier = Modifier.weight(1f),
                            color = if (task.done) Color.Gray else Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = when (task.priority) { Priority.HIGH -> Color(0xFFFFEBEB); Priority.MEDIUM -> Color(0xFFFFF8E1); Priority.LOW -> Color(0xFFE8F5E9) }
                        ) {
                            Text(
                                task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = when (task.priority) { Priority.HIGH -> Color(0xFFE53935); Priority.MEDIUM -> Color(0xFFFFC107); Priority.LOW -> Color(0xFF4CAF50) },
                                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("×", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.clickable { tasks.remove(task) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
//        StudyBottomNav(selected = 2)
//    }
}

@Preview(showBackground = true)
@Composable
fun PlanPreview() {
    StudyOSTheme {
        PlanBody()
    }
}