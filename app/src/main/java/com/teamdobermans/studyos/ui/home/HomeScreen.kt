package com.teamdobermans.studyos.ui.home

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.model.VisionGoalModel
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateStudy: () -> Unit = {},
    onNavigatePlan: () -> Unit = {},
    onNavigateFocus: () -> Unit = {},
    onNavigateFlashcards: () -> Unit = {},
    onNavigateNotes: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName
        ?: user?.email?.substringBefore("@")
        ?: "Learner"

    HomeScreenContent(
        userName = userName,
        greeting = greetingByHour(),
        studyStreak = state.studyStreak,
        weeklyHours = state.weeklyHours,
        lastSubject = state.lastSubject,
        lastNote = state.lastNote,
        tasksCompleted = state.tasksCompleted,
        totalTasks = state.totalTasks,
        flashcardsDueToday = state.flashcardsDueToday,
        dailyGoalPercent = state.dailyGoalPercent,
        todayStudyMinutes = state.todayStudyMinutes,
        goals = state.goals,
        onNavigateStudy = onNavigateStudy,
        onNavigatePlan = onNavigatePlan,
        onNavigateFocus = onNavigateFocus,
        onNavigateFlashcards = onNavigateFlashcards,
        onNavigateNotes = onNavigateNotes
    )
}

@Composable
private fun HomeScreenContent(
    userName: String,
    greeting: String,
    studyStreak: Int,
    weeklyHours: Float,
    lastSubject: String,
    lastNote: String,
    tasksCompleted: Int,
    totalTasks: Int,
    flashcardsDueToday: Int,
    dailyGoalPercent: Float,
    todayStudyMinutes: Int,
    goals: List<VisionGoalModel>,
    onNavigateStudy: () -> Unit = {},
    onNavigatePlan: () -> Unit = {},
    onNavigateFocus: () -> Unit = {},
    onNavigateFlashcards: () -> Unit = {},
    onNavigateNotes: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(
            userName = userName,
            greeting = greeting,
            streak = studyStreak,
            weeklyHours = weeklyHours
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            Spacer(modifier = Modifier.height(16.dp))

            ContinueStudyingCard(
                subject = lastSubject,
                noteTitle = lastNote,
                onClick = onNavigateStudy
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TodayTasksCard(
                    modifier = Modifier.weight(1f),
                    completed = tasksCompleted,
                    total = totalTasks,
                    onClick = onNavigatePlan
                )
                FlashcardReviewCard(
                    modifier = Modifier.weight(1f),
                    dueCount = flashcardsDueToday,
                    onClick = onNavigateFlashcards
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionsRow(
                onNoteClick = onNavigateNotes,
                onTaskClick = onNavigatePlan,
                onFlashcardClick = onNavigateFlashcards,
                onPomodoroClick = onNavigateFocus
            )

            Spacer(modifier = Modifier.height(12.dp))

            DailyProgressCard(
                percent = dailyGoalPercent,
                tasksCompleted = tasksCompleted,
                studyMinutes = todayStudyMinutes
            )

            if (goals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                VisionBoardWidget(goals = goals)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHeader(userName: String, greeting: String, streak: Int, weeklyHours: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(StudyPurpleDeep, StudyPurple)
                )
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = greeting,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Whatshot,
                    label = "Streak",
                    value = "$streak days"
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Schedule,
                    label = "This week",
                    value = "${weeklyHours}h studied"
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContinueStudyingCard(subject: String, noteTitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPurple)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Studying",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = noteTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subject,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TodayTasksCard(
    modifier: Modifier = Modifier,
    completed: Int,
    total: Int,
    onClick: () -> Unit
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0EEFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = StudyPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Today's Tasks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$completed / $total",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = StudyPurple,
                trackColor = StudyPurpleLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(total - completed)} remaining",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FlashcardReviewCard(
    modifier: Modifier = Modifier,
    dueCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Style,
                        contentDescription = null,
                        tint = Color(0xFFE07B39),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Reviews Due",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$dueCount",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (dueCount > 0) Color(0xFFE07B39) else TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF0E0)
            ) {
                Text(
                    text = if (dueCount > 0) "Review now →" else "All caught up!",
                    color = Color(0xFFE07B39),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNoteClick: () -> Unit,
    onTaskClick: () -> Unit,
    onFlashcardClick: () -> Unit,
    onPomodoroClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Quick Actions",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Rounded.EditNote,
                    label = "New Note",
                    bgColor = Color(0xFFF0EEFF),
                    tint = StudyPurple,
                    onClick = onNoteClick
                )
                QuickActionButton(
                    icon = Icons.Rounded.AddTask,
                    label = "Add Task",
                    bgColor = Color(0xFFE8F5E9),
                    tint = Color(0xFF1D9E75),
                    onClick = onTaskClick
                )
                QuickActionButton(
                    icon = Icons.Rounded.Style,
                    label = "Flashcard",
                    bgColor = Color(0xFFFFF0E0),
                    tint = Color(0xFFE07B39),
                    onClick = onFlashcardClick
                )
                QuickActionButton(
                    icon = Icons.Rounded.Timer,
                    label = "Focus",
                    bgColor = Color(0xFFFFE8EE),
                    tint = Color(0xFFE04F7B),
                    onClick = onPomodoroClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DailyProgressCard(
    percent: Float,
    tasksCompleted: Int,
    studyMinutes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily Progress",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${(percent * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudyPurple
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = StudyPurple,
                trackColor = StudyPurpleLight
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProgressStat(label = "Tasks Done", value = "$tasksCompleted")
                ProgressStat(label = "Study Time", value = "${studyMinutes}m")
                ProgressStat(label = "Goal", value = "${(percent * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun ProgressStat(label: String, value: String) {
    Column {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun VisionBoardWidget(goals: List<VisionGoalModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPurpleDeep)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Goals",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            goals.take(3).forEach { goal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = goal.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.text,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        if (goal.targetValue.isNotEmpty()) {
                            Text(
                                text = "Target: ${goal.targetValue}",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (goals.size > 3) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+ ${goals.size - 3} more goals",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun greetingByHour(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StudyOSTheme {
        HomeScreenContent(
            userName = "Alex",
            greeting = "Good Afternoon",
            studyStreak = 12,
            weeklyHours = 15.5f,
            lastSubject = "Mobile Development",
            lastNote = "Jetpack Compose Basics",
            tasksCompleted = 5,
            totalTasks = 8,
            flashcardsDueToday = 20,
            dailyGoalPercent = 0.65f,
            todayStudyMinutes = 120,
            goals = emptyList()
        )
    }
}
