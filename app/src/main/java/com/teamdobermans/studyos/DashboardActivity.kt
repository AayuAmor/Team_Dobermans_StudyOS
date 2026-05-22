package com.teamdobermans.studyos

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody(viewModel = dashboardViewModel)
        }
    }
}

@Composable
fun DashboardBody(viewModel: DashboardViewModel) {
    val context = LocalContext.current

    val progress by viewModel.progress.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

    var localTimerRunning by remember { mutableStateOf(false) }
    var localTimeLeft by remember { mutableLongStateOf(25 * 60L) }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Scaffold(
        bottomBar = { StudyOSBottomNav(currentRoute = NavRoute.HOME, context = context) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F3FF))
                .verticalScroll(rememberScrollState())
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6B63D5))
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            text = "Ditya 👋",
                            style = TextStyle(
                                fontSize = 17.sp,
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
                            .clickable {
                                context.startActivity(Intent(context, ProfileActivity::class.java))
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable {
                            Toast.makeText(context, "15 day streak! Keep going 🔥", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Study Streak",
                            style = TextStyle(fontSize = 13.sp, color = Color.White)
                        )
                    }
                    Text(
                        text = "15 days",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Text(
                text = "Today's Progress",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E)
                ),
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
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
                                color = Color(0xFF1A1A2E)
                            )
                        )
                        Text(
                            text = "${progress.toInt()}%",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B63D5)
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
                        color = Color(0xFF6B63D5),
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Text(
                text = "Pomodoro Timer",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E)
                ),
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6B63D5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (localTimerRunning) "Running..." else "Focus Session",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            text = "%02d:%02d".format(localTimeLeft / 60, localTimeLeft % 60),
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
                            id = if (localTimerRunning) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
                        ),
                        contentDescription = "Play/Pause",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(8.dp)
                            .clickable {
                                localTimerRunning = !localTimerRunning
                            }
                    )
                }
            }

            Text(
                text = "Study Tools",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E)
                ),
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 8.dp)
            )

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
                    subtitle = "8 sets",
                    onClick = { context.startActivity(Intent(context, Flashcards::class.java)) }
                )
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_quiz_24,
                    iconBg = Color(0xFFF0E8FF),
                    iconTint = Color(0xFF7B4FE0),
                    name = "Quiz Mode",
                    subtitle = "13 questions",
                    onClick = { context.startActivity(Intent(context, QuizScreen::class.java)) }
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
                    subtitle = "6 min break",
                    onClick = { context.startActivity(Intent(context, BrainGameScreen::class.java)) }
                )
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_video_library_24,
                    iconBg = Color(0xFFE8F4FF),
                    iconTint = Color(0xFF3A82E0),
                    name = "Videos to Notes",
                    subtitle = "Convert into notes",
                    onClick = { context.startActivity(Intent(context, VideotoNotes::class.java)) }
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
                    subtitle = "10 min",
                    onClick = { context.startActivity(Intent(context, MockTestActivity::class.java)) }
                )
                ToolCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.baseline_bar_chart_24,
                    iconBg = Color(0xFFE8EAFF),
                    iconTint = Color(0xFF4B5BE0),
                    name = "Vision Board",
                    subtitle = "Your goals",
                    onClick = { context.startActivity(Intent(context, VisionBoardActivity::class.java)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
        modifier = modifier.clickable { onClick() },
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
                    color = Color(0xFF1A1A2E)
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            )
        }
    }
}

@Preview
@Composable
fun DashboardPreview() {
    DashboardBody(viewModel = DashboardViewModel())
}