package com.teamdobermans.studyos.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AnalyticsUiState
import com.teamdobermans.studyos.viewModel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        AnalyticsHeader(onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            HeroMetricsRow(state = state)

            Spacer(modifier = Modifier.height(14.dp))

            WeeklyBarChartCard(data = state.weeklyData)

            Spacer(modifier = Modifier.height(14.dp))

            SubjectBreakdownCard(items = state.subjectBreakdown)

            Spacer(modifier = Modifier.height(14.dp))

            FocusScoreCard(score = state.focusScore)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnalyticsHeader(onBack: () -> Unit) {
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
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Analytics", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Your study performance overview", color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun HeroMetricsRow(state: AnalyticsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Rounded.Whatshot,
            iconColor = Color(0xFFE04F7B),
            value     = "${state.studyStreak}",
            unit      = "days",
            label     = "Streak"
        )
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Rounded.Schedule,
            iconColor = Color(0xFF3A82E0),
            value     = "${state.monthlyHours.toInt()}",
            unit      = "hrs",
            label     = "This Month"
        )
        MetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Rounded.EmojiEvents,
            iconColor = Color(0xFFE07B39),
            value     = "${(state.quizAccuracy * 100).toInt()}%",
            unit      = "",
            label     = "Quiz Acc."
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    unit: String,
    label: String
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(14.dp)),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = unit, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun WeeklyBarChartCard(data: List<com.teamdobermans.studyos.viewModel.WeeklyBarData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.BarChart, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Weekly Study Hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            WeeklyBarChart(data = data)
        }
    }
}

@Composable
private fun WeeklyBarChart(data: List<com.teamdobermans.studyos.viewModel.WeeklyBarData>) {
    val maxHours = data.maxOfOrNull { it.hours } ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { entry ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                if (entry.hours > 0f) {
                    Text(
                        text  = "${entry.hours.toInt()}h",
                        fontSize = 9.sp,
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val barFraction = if (maxHours > 0f) entry.hours / maxHours else 0f
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(barFraction.coerceAtLeast(0.03f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (entry.hours > 0f) StudyPurple else StudyPurpleLight
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = entry.day.take(3),
                    fontSize = 9.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SubjectBreakdownCard(items: List<com.teamdobermans.studyos.viewModel.SubjectBreakdownItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.PieChart, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Subject Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(14.dp))
            items.forEach { item ->
                val color = Color(item.colorArgb)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Text("${(item.percent * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(7.dp)),
                        color      = color,
                        trackColor = color.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun FocusScoreCard(score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = StudyPurpleDeep)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Focus Score", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$score", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    Text("/100", color = Color.White.copy(alpha = 0.55f), fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = scoreLabel(score),
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            FocusScoreRing(score = score)
        }
    }
}

@Composable
private fun FocusScoreRing(score: Int) {
    val fraction = score / 100f
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.White.copy(alpha = 0.15f), style = Stroke(width = 10.dp.toPx()))
            drawArc(
                color      = Color(0xFFFFD700),
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter  = false,
                style      = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(text = "$score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

private fun scoreLabel(score: Int) = when {
    score >= 85 -> "Excellent"
    score >= 70 -> "Great work!"
    score >= 50 -> "Good effort"
    else        -> "Keep going!"
}

