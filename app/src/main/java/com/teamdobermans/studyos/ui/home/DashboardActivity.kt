package com.teamdobermans.studyos.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.ui.focus.BrainGameActivityShell
import com.teamdobermans.studyos.ui.focus.PomodoroActivity
import com.teamdobermans.studyos.ui.plan.PlanActivity
import com.teamdobermans.studyos.ui.profile.ProfileActivity
import com.teamdobermans.studyos.ui.profile.ProgressActivity
import com.teamdobermans.studyos.ui.study.Flashcards
import com.teamdobermans.studyos.ui.study.MockTestActivity
import com.teamdobermans.studyos.ui.study.QuizScreen
import com.teamdobermans.studyos.ui.study.VideotoNotes
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.DashboardViewModel

class DashboardActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DashboardBody(
                viewModel = dashboardViewModel,
                onNavigatePomodoro = {
                    startActivity(
                        Intent(this, PomodoroActivity::class.java)
                            .putExtra("auto_start", true)
                    )
                },
                onNavigateVisionBoard = {
                    startActivity(Intent(this, VisionBoardActivity::class.java))
                },
                onNavigateAnalytics = {
                    startActivity(Intent(this, ProgressActivity::class.java))
                },
                onNavigateProfile = {
                    startActivity(Intent(this, ProfileActivity::class.java))
                },
                onNavigateFlashcards = {
                    startActivity(Intent(this, Flashcards::class.java))
                },
                onNavigateQuiz = {
                    startActivity(Intent(this, QuizScreen::class.java))
                },
                onNavigateBrainGame = {
                    startActivity(Intent(this, BrainGameActivityShell::class.java))
                },
                onNavigateVideoNotes = {
                    startActivity(Intent(this, VideotoNotes::class.java))
                },
                onNavigateMockTest = {
                    startActivity(Intent(this, MockTestActivity::class.java))
                },
                onNavigatePlan = {
                    startActivity(Intent(this, PlanActivity::class.java))
                },
                onNavigateNotes = {
                    Toast.makeText(this, "Notes section is not connected yet", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun DashboardBody(
    viewModel: DashboardViewModel,
    onNavigatePomodoro: () -> Unit = {},
    onNavigateVisionBoard: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateAnalytics: () -> Unit = {},
    onNavigateFlashcards: () -> Unit = {},
    onNavigateQuiz: () -> Unit = {},
    onNavigateBrainGame: () -> Unit = {},
    onNavigateVideoNotes: () -> Unit = {},
    onNavigateMockTest: () -> Unit = {},
    onNavigatePlan: () -> Unit = {},
    onNavigateNotes: () -> Unit = {}
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"

    val dashboardState by viewModel.state.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
        viewModel.onSessionComplete = {
            Toast.makeText(context, "Session complete!", Toast.LENGTH_LONG).show()
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DashboardHeader(
                userName = userName,
                streakCount = dashboardState.streakCount,
                weeklyHours = dashboardState.weeklyStudyHours,
                onNavigateProfile = onNavigateProfile
            )

            SectionTitle("Study Summary")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Today",
                    value = "${dashboardState.dailyStudyHours}h"
                )

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Week",
                    value = "${dashboardState.weeklyStudyHours}h"
                )

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak",
                    value = "${dashboardState.streakCount}d"
                )
            }

            SectionTitle("Today's Progress")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Software Development",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )

                        Text(
                            text = "${progress.toInt()}%",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudyPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = StudyPurple,
                        trackColor = StudyPurpleLight
                    )
                }
            }

            SectionTitle("Start Focus Session")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudyPurpleDeep)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (timerRunning) "Running..." else "Focus Session",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )

                            Text(
                                text = timeText,
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Image(
                            painter = painterResource(
                                id = if (timerRunning) {
                                    R.drawable.baseline_pause_24
                                } else {
                                    R.drawable.baseline_play_arrow_24
                                }
                            ),
                            contentDescription = "Play/Pause",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(8.dp)
                                .clickable { viewModel.toggleTimer() }
                        )
                    }

                    Button(
                        onClick = onNavigatePomodoro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            "Start Session",
                            color = StudyPurpleDeep,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            SectionTitle("Today's Tasks")

            TodayTasksCard(
                tasks = dashboardState.todayTasks,
                onNavigatePlan = onNavigatePlan
            )

            SectionTitle("Vision Board")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clickable { onNavigateVisionBoard() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "Vision Board",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Text(
                                "Keep your goals visible while you study",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateVisionBoard,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Open Board",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            SectionTitle("Study Tools")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_credit_card_24,
                    iconBg = Color(0xFFFFF0E0),
                    iconTint = Color(0xFFE07B39),
                    name = "Flash Cards",
                    subtitle = "Review memory",
                    onClick = onNavigateFlashcards
                )

                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_quiz_24,
                    iconBg = Color(0xFFF0E8FF),
                    iconTint = Color(0xFF7B4FE0),
                    name = "Quiz Mode",
                    subtitle = "Test yourself",
                    onClick = onNavigateQuiz
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_sports_esports_24,
                    iconBg = Color(0xFFFFE8EE),
                    iconTint = Color(0xFFE04F7B),
                    name = "Brain Game",
                    subtitle = "Break booster",
                    onClick = onNavigateBrainGame
                )

                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_video_library_24,
                    iconBg = Color(0xFFE8F4FF),
                    iconTint = Color(0xFF3A82E0),
                    name = "Video Notes",
                    subtitle = "Convert video",
                    onClick = onNavigateVideoNotes
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_assignment_24,
                    iconBg = Color(0xFFE8FFF4),
                    iconTint = Color(0xFF1D9E75),
                    name = "Mock Test",
                    subtitle = "Exam practice",
                    onClick = onNavigateMockTest
                )

                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_assignment_24,
                    iconBg = Color(0xFFF1EAFE),
                    iconTint = StudyPurple,
                    name = "Notes",
                    subtitle = "Coming soon",
                    onClick = onNavigateNotes
                )
            }

            SectionTitle("Analytics")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudyPurpleDeep)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Progress",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Image(
                            painter = painterResource(id = R.drawable.baseline_bar_chart_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnalyticsMiniStat(label = "Streak", value = "${dashboardState.streakCount}d")
                        DividerLine()
                        AnalyticsMiniStat(label = "This Week", value = "${dashboardState.weeklyStudyHours}h")
                        DividerLine()
                        AnalyticsMiniStat(label = "Quiz Acc.", value = "${dashboardState.quizAccuracy}%")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateAnalytics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            "View Full Analytics",
                            color = StudyPurpleDeep,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        DashboardBottomNav(
            onHomeClick = {},
            onStudyClick = onNavigateNotes,
            onFocusClick = onNavigatePomodoro,
            onPlanClick = onNavigatePlan,
            onProfileClick = onNavigateProfile
        )
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    streakCount: Int,
    weeklyHours: Float,
    onNavigateProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudyPurpleDeep)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good Morning",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                )

                Text(
                    text = userName,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Image(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable { onNavigateProfile() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Study Streak",
                style = TextStyle(fontSize = 13.sp, color = Color.White)
            )

            Text(
                text = "$streakCount days",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$weeklyHours hours studied this week",
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.3.sp
        ),
        modifier = Modifier.padding(start = 14.dp, top = 16.dp, bottom = 10.dp)
    )
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = StudyPurple,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TodayTasksCard(
    tasks: List<Task>,
    onNavigatePlan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clickable { onNavigatePlan() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All",
                    color = StudyPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks for today",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            } else {
                tasks.take(3).forEach { task ->
                    TaskItem(task = task)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(StudyPurple)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = task.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = task.subjectName,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ToolCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    name: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = name,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }

            Text(
                text = name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )

            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            )
        }
    }
}

@Composable
fun AnalyticsMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            label,
            color = Color.White.copy(alpha = 0.70f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color.White.copy(alpha = 0.25f))
    )
}

@Composable
private fun DashboardBottomNav(
    onHomeClick: () -> Unit,
    onStudyClick: () -> Unit,
    onFocusClick: () -> Unit,
    onPlanClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        BottomNavItem(
            selected = true,
            label = "Home",
            iconText = "⌂",
            onClick = onHomeClick
        )

        BottomNavItem(
            selected = false,
            label = "Study",
            iconText = "📚",
            onClick = onStudyClick
        )

        BottomNavItem(
            selected = false,
            label = "Focus",
            iconText = "⏱",
            onClick = onFocusClick
        )

        BottomNavItem(
            selected = false,
            label = "Plan",
            iconText = "📅",
            onClick = onPlanClick
        )

        BottomNavItem(
            selected = false,
            label = "Profile",
            iconText = "👤",
            onClick = onProfileClick
        )
    }
}

@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    label: String,
    iconText: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Text(
                text = iconText,
                fontSize = 18.sp
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = StudyPurple,
            selectedTextColor = StudyPurple,
            indicatorColor = StudyPurpleLight,
            unselectedIconColor = StudyPurple.copy(alpha = 0.45f),
            unselectedTextColor = StudyPurple.copy(alpha = 0.45f)
        )
    )
}

@Preview
@Composable
fun DashboardPreview() {
    DashboardBody(
        viewModel = DashboardViewModel()
    )
}