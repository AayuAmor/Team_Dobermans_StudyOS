package com.teamdobermans.studyos.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.WeeklyTrendPoint
import com.teamdobermans.studyos.model.WeeklyTrendType
import com.teamdobermans.studyos.ui.theme.*
import java.util.Calendar

@Composable
fun WeeklyTrendBarChart(
    points: List<WeeklyTrendPoint>,
    trendType: WeeklyTrendType,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxValue = remember(points) {
        points.maxOfOrNull { it.value }?.coerceAtLeast(0.001f) ?: 1f
    }
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        points.forEach { point ->
            TrendBarItem(
                point = point,
                maxValue = maxValue,
                isToday = point.dateMillis == todayStart,
                trendType = trendType,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TrendBarItem(
    point: WeeklyTrendPoint,
    maxValue: Float,
    isToday: Boolean,
    trendType: WeeklyTrendType,
    modifier: Modifier = Modifier
) {
    val fraction = (point.value / maxValue).coerceIn(0f, 1f)
    val barColor = if (isToday) StudyPurpleDeep else StudyPurple

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value label above bar (hidden when zero)
        Text(
            text = if (point.value > 0f) formatBarLabel(point.value, trendType) else "",
            fontSize = 7.sp,
            color = if (isToday) StudyPurple else TextSecondary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))

        // Bar + track
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background track — always visible so chart structure is clear at zero values
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudyPurpleLight.copy(alpha = 0.65f))
            )
            // Filled bar
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .fillMaxHeight(fraction.coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day label
        Text(
            text = point.dayLabel,
            fontSize = 9.sp,
            color = if (isToday) StudyPurple else TextSecondary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatBarLabel(value: Float, type: WeeklyTrendType): String = when (type) {
    WeeklyTrendType.STUDY_TIME      -> if (value >= 60) "${"%.0f".format(value / 60)}h" else "${value.toInt()}m"
    WeeklyTrendType.TASKS_COMPLETED -> "${value.toInt()}"
    WeeklyTrendType.QUIZ_SCORE      -> "${"%.0f".format(value)}%"
    WeeklyTrendType.REVIEW_COUNT    -> "${value.toInt()}"
}
