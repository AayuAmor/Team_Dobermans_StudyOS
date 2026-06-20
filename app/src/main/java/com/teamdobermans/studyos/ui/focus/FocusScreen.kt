package com.teamdobermans.studyos.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.util.UUID
import com.teamdobermans.studyos.model.Priority
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.PomodoroTab
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.FocusUiState
import com.teamdobermans.studyos.viewModel.FocusViewModel
import com.teamdobermans.studyos.viewModel.PomodoroViewModel

@Composable
fun FocusScreen(
    focusViewModel: FocusViewModel,
    onNavigateBrainGame: () -> Unit = {}
) {
    val pomodoroVm: PomodoroViewModel = viewModel()
    val focusState by focusViewModel.state.collectAsState()
    val tasks by focusViewModel.tasks.collectAsState()
    val selectedTask by focusViewModel.selectedTask.collectAsState()

    val selectedTab by pomodoroVm.selectedTab.collectAsState()
    val focusMinutes by pomodoroVm.focusMinutes.collectAsState()
    val shortMinutes by pomodoroVm.shortMinutes.collectAsState()
    val longMinutes by pomodoroVm.longMinutes.collectAsState()
    val isRunning by pomodoroVm.isRunning.collectAsState()
    val sessionsToday by pomodoroVm.sessionsToday.collectAsState()
    val timeRemaining by pomodoroVm.timeRemaining.collectAsState()

    val totalSeconds: Long = when (selectedTab) {
        PomodoroTab.FOCUS -> (focusMinutes * 60).toLong()
        PomodoroTab.SHORT_BREAK -> (shortMinutes * 60).toLong()
        PomodoroTab.LONG_BREAK -> (longMinutes * 60).toLong()
    }

    FocusContent(
        focusState = focusState,
        tasks = tasks,
        selectedTask = selectedTask,
        selectedTab = selectedTab,
        isRunning = isRunning,
        sessionsToday = sessionsToday,
        timeRemaining = timeRemaining,
        totalSeconds = totalSeconds,
        onTabSelect = pomodoroVm::selectTab,
        onToggleTimer = pomodoroVm::toggleTimer,
        onResetTimer = pomodoroVm::resetTimer,
        onTaskSelect = focusViewModel::selectTask,
        onClearTask = focusViewModel::clearSelectedTask,
        onSoundTap = focusViewModel::toggleSound,
        onNavigateBrainGame = onNavigateBrainGame,
        onCompleteSession = { focusViewModel.completeSession(focusMinutes) }
    )
}

@Composable
fun FocusContent(
    focusState: FocusUiState,
    tasks: List<Task>,
    selectedTask: Task?,
    selectedTab: PomodoroTab,
    isRunning: Boolean,
    sessionsToday: Int,
    timeRemaining: Long,
    totalSeconds: Long,
    onTabSelect: (PomodoroTab) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onTaskSelect: (Task) -> Unit,
    onClearTask: () -> Unit,
    onSoundTap: (String) -> Unit,
    onNavigateBrainGame: () -> Unit,
    onCompleteSession: () -> Unit
) {
    val timerProgress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) timeRemaining.toFloat() / totalSeconds else 1f,
        animationSpec = tween(500),
        label = "focusTimerProgress"
    )
    val timeLabel = "%02d:%02d".format(timeRemaining / 60, timeRemaining % 60)
    val modeLabel = when (selectedTab) {
        PomodoroTab.FOCUS -> "FOCUS"
        PomodoroTab.SHORT_BREAK -> "SHORT BREAK"
        PomodoroTab.LONG_BREAK -> "LONG BREAK"
    }

    var lastProcessedSessionCount by remember { mutableIntStateOf(sessionsToday) }

    LaunchedEffect(sessionsToday) {
        if (sessionsToday > lastProcessedSessionCount && selectedTab == PomodoroTab.FOCUS) {
            onCompleteSession()
            lastProcessedSessionCount = sessionsToday
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        FocusHeader(
            sessionsToday = sessionsToday,
            selectedTab = selectedTab,
            onTabSelect = onTabSelect
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeroTimerCard(
                progress = timerProgress,
                timeLabel = timeLabel,
                modeLabel = modeLabel,
                isRunning = isRunning,
                onToggle = onToggleTimer,
                onReset = onResetTimer
            )

            Spacer(modifier = Modifier.height(14.dp))

            TaskPickerCard(
                tasks = tasks,
                selectedTask = selectedTask,
                onTaskSelect = onTaskSelect,
                onClear = onClearTask
            )

            Spacer(modifier = Modifier.height(14.dp))

            SessionDotsCard(sessionsToday = sessionsToday)

            Spacer(modifier = Modifier.height(14.dp))

            FocusSoundsSection(
                activeSound = focusState.activeSound,
                onSoundTap = onSoundTap
            )

            Spacer(modifier = Modifier.height(14.dp))

            BrainGamesSection(onNavigateBrainGame = onNavigateBrainGame)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskPickerCard(
    tasks: List<Task>,
    selectedTask: Task?,
    onTaskSelect: (Task) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp)),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.Task,
                    contentDescription = null,
                    tint               = StudyPurple,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text     = "Studying for",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                    Text(
                        text       = selectedTask?.title ?: "Select a task",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (selectedTask != null) TextPrimary else TextSecondary
                    )
                }
            }
            if (selectedTask != null) {
                Icon(
                    imageVector        = Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint               = TextSecondary,
                    modifier           = Modifier
                        .size(18.dp)
                        .clickable { onClear() }
                )
            } else {
                Icon(
                    imageVector        = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint               = StudyPurple,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (tasks.isEmpty()) {
                DropdownMenuItem(
                    text    = { Text("No tasks found", color = TextSecondary) },
                    onClick = { expanded = false }
                )
            } else {
                tasks.forEach { task ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text       = task.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp
                                )
                                Text(
                                    text     = task.subjectName,
                                    fontSize = 11.sp,
                                    color    = TextSecondary
                                )
                            }
                        },
                        onClick = {
                            onTaskSelect(task)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusHeader(
    sessionsToday: Int,
    selectedTab: PomodoroTab,
    onTabSelect: (PomodoroTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Focus", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text     = "Notifications silenced during focus",
                        color    = Color.White.copy(alpha = 0.70f),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.20f)
                ) {
                    Text(
                        text       = "$sessionsToday / 4",
                        color      = Color.White,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Surface(shape = RoundedCornerShape(50.dp), color = Color.White.copy(alpha = 0.15f)) {
                Row(modifier = Modifier.fillMaxWidth().padding(3.dp)) {
                    PomodoroTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Surface(
                            shape    = RoundedCornerShape(50.dp),
                            color    = if (isSelected) Color.White else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { onTabSelect(tab) }
                        ) {
                            Text(
                                text = when (tab) {
                                    PomodoroTab.FOCUS       -> "Focus"
                                    PomodoroTab.SHORT_BREAK -> "Short"
                                    PomodoroTab.LONG_BREAK  -> "Long"
                                },
                                modifier   = Modifier.padding(vertical = 8.dp),
                                color      = if (isSelected) StudyPurple else Color.White,
                                fontSize   = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign  = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroTimerCard(
    progress: Float,
    timeLabel: String,
    modeLabel: String,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = StudyPurpleLight, style = Stroke(width = 16.dp.toPx()))
                    drawArc(
                        color      = StudyPurple,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter  = false,
                        style      = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = timeLabel,
                        color         = TextPrimary,
                        fontSize      = 44.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(text = modeLabel, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = onToggle,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(24.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Icon(
                        imageVector        = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text       = if (isRunning) "Pause" else "Start",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick  = onReset,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(24.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StudyPurple),
                    border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.5.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SessionDotsCard(sessionsToday: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp)),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sessions today", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("$sessionsToday of 4 completed", fontSize = 11.sp, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (index < sessionsToday) StudyPurple else StudyPurpleLight)
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusSoundsSection(activeSound: String?, onSoundTap: (String) -> Unit) {
    val sounds = listOf(
        Triple("rain",       Icons.Rounded.WaterDrop, "Rain"),
        Triple("whitenoise", Icons.Rounded.Air,        "White Noise"),
        Triple("cafe",       Icons.Rounded.LocalCafe,  "Café")
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint               = StudyPurple,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Focus Sounds", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sounds.forEach { (id, icon, label) ->
                    SoundChip(
                        modifier = Modifier.weight(1f),
                        icon     = icon,
                        label    = label,
                        isActive = activeSound == id,
                        onClick  = { onSoundTap(id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) StudyPurple else StudyPurpleFaint)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = if (isActive) Color.White else StudyPurple,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = label,
            color      = if (isActive) Color.White else TextPrimary,
            fontSize   = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BrainGamesSection(onNavigateBrainGame: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.SportsEsports,
                    contentDescription = null,
                    tint               = StudyPurple,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Brain Games", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Text(
                text       = "View all",
                color      = StudyPurple,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.clickable { onNavigateBrainGame() }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrainGameCard(
                modifier   = Modifier.weight(1f),
                icon       = Icons.Rounded.GridView,
                iconBg     = Color(0xFFFFE8EE),
                iconTint   = Color(0xFFE04F7B),
                title      = "Memory Match",
                difficulty = "Easy",
                onClick    = onNavigateBrainGame
            )
            BrainGameCard(
                modifier   = Modifier.weight(1f),
                icon       = Icons.Rounded.Calculate,
                iconBg     = Color(0xFFE8EAFF),
                iconTint   = Color(0xFF4B5BE0),
                title      = "Math Sprint",
                difficulty = "Medium",
                onClick    = onNavigateBrainGame
            )
        }
    }
}

@Composable
private fun BrainGameCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    difficulty: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title,      fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = difficulty, fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = iconBg) {
                Text(
                    text       = "Play →",
                    color      = iconTint,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FocusScreenPreview() {
    StudyOSTheme {
        var selectedTab by remember { mutableStateOf(PomodoroTab.FOCUS) }
        var isRunning by remember { mutableStateOf(false) }
        var sessionsCompleted by remember { mutableIntStateOf(2) }

        FocusContent(
            focusState = FocusUiState(activeSound = "rain"),
            tasks = listOf(
                Task(
                    id = "1",
                    title = "Math Homework",
                    description = "",
                    startDate = LocalDate.now(),
                    endDate = null,
                    priority = Priority.HIGH,
                    subjectId = "s1",
                    subjectName = "Calculus"
                )
            ),
            selectedTask = Task(
                id = "1",
                title = "Math Homework",
                description = "",
                startDate = LocalDate.now(),
                endDate = null,
                priority = Priority.HIGH,
                subjectId = "s1",
                subjectName = "Calculus"
            ),
            selectedTab = selectedTab,
            isRunning = isRunning,
            sessionsToday = sessionsCompleted,
            timeRemaining = 1125L,
            totalSeconds = 1500L,
            onTabSelect = { selectedTab = it },
            onToggleTimer = { isRunning = !isRunning },
            onResetTimer = { isRunning = false },
            onTaskSelect = {},
            onClearTask = {},
            onSoundTap = {},
            onNavigateBrainGame = {},
            onCompleteSession = {}
        )
    }
}
