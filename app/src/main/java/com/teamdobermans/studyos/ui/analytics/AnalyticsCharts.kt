package com.teamdobermans.studyos.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.SubjectBreakdownItem
import com.teamdobermans.studyos.viewModel.WeeklyBarData

@Composable
fun WeeklyBarChartCard(data: List<WeeklyBarData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
fun WeeklyBarChart(data: List<WeeklyBarData>) {
    val maxHours = data.maxOfOrNull { it.hours } ?: 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.Bottom
    ) {
        data.forEach { entry ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier            = Modifier.weight(1f)
            ) {
                if (entry.hours > 0f) {
                    Text(text = "${entry.hours.toInt()}h", fontSize = 9.sp, color = StudyPurple, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                val barFraction = if (maxHours > 0f) entry.hours / maxHours else 0f
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(0.55f)
                        .fillMaxHeight(barFraction.coerceAtLeast(0.03f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (entry.hours > 0f) StudyPurple else StudyPurpleLight)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = entry.day.take(3), fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun SubjectBreakdownCard(items: List<SubjectBreakdownItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        verticalAlignment     = Alignment.CenterVertically
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
                        progress   = { item.percent },
                        modifier   = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(7.dp)),
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
fun QuizPerformanceLineChartCard(scores: List<Float>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.ShowChart, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Quiz Performance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            QuizLineChart(data = scores)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(day.take(3), fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun QuizLineChart(data: List<Float>) {
    val lineColor = StudyPurple
    val dotFill   = StudyPurpleLight
    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (data.size < 2) return@Canvas
            val maxVal = data.maxOrNull() ?: 1f
            val minVal = data.minOrNull() ?: 0f
            val range  = (maxVal - minVal).coerceAtLeast(0.01f)
            val stepX  = size.width / (data.size - 1)
            val path   = Path()
            data.forEachIndexed { i, value ->
                val x = i * stepX
                val y = size.height - ((value - minVal) / range) * (size.height * 0.80f) - (size.height * 0.10f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            data.forEachIndexed { i, value ->
                val x = i * stepX
                val y = size.height - ((value - minVal) / range) * (size.height * 0.80f) - (size.height * 0.10f)
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                drawCircle(color = dotFill,   radius = 2.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

@Composable
fun FocusScoreCard(score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPurpleDeep)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
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
                Text(focusScoreLabel(score), color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            FocusScoreRing(score = score)
        }
    }
}

@Composable
fun FocusScoreRing(score: Int) {
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
        Text("$score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

fun focusScoreLabel(score: Int) = when {
    score >= 85 -> "Excellent"
    score >= 70 -> "Great work!"
    score >= 50 -> "Good effort"
    else        -> "Keep going!"
}
