package com.teamdobermans.studyos.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.ui.analytics.*
import com.teamdobermans.studyos.ui.components.AnalyticsHeroRow
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AnalyticsViewModel
import com.teamdobermans.studyos.viewModel.SessionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressActivity : ComponentActivity() {
    private val analyticsViewModel: AnalyticsViewModel by viewModels()
    private val sessionHistoryViewModel: SessionHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProgressBody(
                analyticsViewModel = analyticsViewModel,
                sessionHistoryViewModel = sessionHistoryViewModel,
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun ProgressBody(
    analyticsViewModel: AnalyticsViewModel = AnalyticsViewModel(),
    sessionHistoryViewModel: SessionHistoryViewModel = SessionHistoryViewModel(),
    onBack: () -> Unit = {}
) {
    val state by analyticsViewModel.state.collectAsState()
    val sessions by sessionHistoryViewModel.sessions.collectAsState()
    val isLoadingSessions by sessionHistoryViewModel.isLoading.collectAsState()

    val bestDay    = state.weeklyData.maxByOrNull { it.hours }?.day ?: "Thursday"
    val strongest  = state.subjectBreakdown.maxByOrNull { it.percent }?.name ?: "Mathematics"
    val weakest    = state.subjectBreakdown.minByOrNull { it.percent }?.name ?: "Chemistry"
    val taskPct    = (state.focusScore * 0.85f).toInt().coerceIn(0, 100)
    val quizScores = listOf(0.60f, 0.72f, 0.65f, 0.78f, 0.74f, 0.82f, 0.78f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        ProgressHeader(onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            AnalyticsHeroRow(state = state)

            Spacer(modifier = Modifier.height(14.dp))

            WeeklyBarChartCard(data = state.weeklyData)

            Spacer(modifier = Modifier.height(14.dp))

            SubjectBreakdownCard(items = state.subjectBreakdown)

            Spacer(modifier = Modifier.height(14.dp))

            QuizPerformanceLineChartCard(scores = quizScores)

            Spacer(modifier = Modifier.height(14.dp))

            FocusScoreCard(score = state.focusScore)

            Spacer(modifier = Modifier.height(14.dp))

            TaskCompletionCard(percent = taskPct)

            Spacer(modifier = Modifier.height(14.dp))

            RecentSessionsCard(sessions = sessions, isLoading = isLoadingSessions)

            Spacer(modifier = Modifier.height(14.dp))

            QuickInsightsCard(
                bestDay     = bestDay,
                strongest   = strongest,
                weakest     = weakest,
                improvement = "+12%"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProgressHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Progress & Analytics", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Your study performance overview", color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun TaskCompletionCard(percent: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Task Completion", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text("$percent%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = StudyPurple)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress   = { percent / 100f },
                modifier   = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                color      = StudyPurple,
                trackColor = StudyPurpleLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("$percent of 100 tasks completed this week", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

private fun formatTimeAgo(completedAt: Long): String {
    val diff = System.currentTimeMillis() - completedAt
    return when {
        diff < 60_000L         -> "Just now"
        diff < 3_600_000L      -> "${diff / 60_000}m ago"
        diff < 86_400_000L     -> "${diff / 3_600_000}h ago"
        diff < 172_800_000L    -> "Yesterday"
        else                   -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(completedAt))
    }
}

@Composable
private fun RecentSessionsCard(
    sessions: List<FocusSessionModel>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recent Study Sessions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StudyPurple, modifier = Modifier.size(24.dp))
                    }
                }
                sessions.isEmpty() -> {
                    Text(
                        text     = "No sessions recorded yet. Complete a focus session to see your history here.",
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }
                else -> {
                    val displaySessions = sessions.take(10)
                    displaySessions.forEachIndexed { index, session ->
                        SessionRow(
                            subject  = session.taskTitle.ifBlank { "Focus Session" },
                            duration = "${session.durationMinutes} min",
                            timeAgo  = formatTimeAgo(session.completedAt)
                        )
                        if (index < displaySessions.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = StudyPurpleFaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(subject: String, duration: String, timeAgo: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StudyPurpleFaint),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(subject,  fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(duration, fontSize = 11.sp, color = TextSecondary)
        }
        Text(timeAgo, fontSize = 11.sp, color = TextHint)
    }
}

@Composable
private fun QuickInsightsCard(bestDay: String, strongest: String, weakest: String, improvement: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.Lightbulb, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Quick Insights", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            InsightRow(icon = Icons.Rounded.CalendarToday, label = "Best Study Day",     value = bestDay,     tint = Color(0xFF3A82E0))
            Spacer(modifier = Modifier.height(10.dp))
            InsightRow(icon = Icons.Rounded.TrendingUp,    label = "Strongest Subject",   value = strongest,   tint = Color(0xFF1D9E75))
            Spacer(modifier = Modifier.height(10.dp))
            InsightRow(icon = Icons.Rounded.TrendingDown,  label = "Weakest Subject",     value = weakest,     tint = Color(0xFFE04F7B))
            Spacer(modifier = Modifier.height(10.dp))
            InsightRow(icon = Icons.Rounded.ShowChart,     label = "Weekly Improvement",  value = improvement, tint = Color(0xFFE07B39))
        }
    }
}

@Composable
private fun InsightRow(icon: ImageVector, label: String, value: String, tint: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressPreview() {
    ProgressBody()
}
