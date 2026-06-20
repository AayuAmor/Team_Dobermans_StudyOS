package com.teamdobermans.studyos.ui.analytics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.WeeklyTrendType
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AnalyticsViewModel

@Composable
fun WeeklyTrendCard(
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by analyticsViewModel.state.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Trends",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Track your learning activity this week",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = StudyPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trend type selector chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeeklyTrendType.entries.forEach { type ->
                    FilterChip(
                        selected = state.selectedTrendType == type,
                        onClick = { analyticsViewModel.selectTrendType(type) },
                        label = { Text(type.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudyPurple,
                            selectedLabelColor = Color.White,
                            containerColor = StudyPurpleFaint,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total this week — only shown when data is available
            if (!state.isLoading && !state.isEmpty) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatWeekTotal(state.totalThisWeek, state.selectedTrendType),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "this week",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chart or loading state
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StudyPurple,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                else -> {
                    // Chart always renders — shows background tracks even at zero values
                    if (state.weeklyTrendPoints.isNotEmpty()) {
                        WeeklyTrendBarChart(
                            points = state.weeklyTrendPoints,
                            trendType = state.selectedTrendType
                        )
                    }

                    // Empty state message below the chart
                    if (state.isEmpty) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No activity recorded this week",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete study sessions, tasks, or quizzes to see your weekly trends.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatWeekTotal(value: Float, type: WeeklyTrendType): String = when (type) {
    WeeklyTrendType.STUDY_TIME      -> if (value >= 60) "${"%.1f".format(value / 60)}h" else "${value.toInt()}m"
    WeeklyTrendType.TASKS_COMPLETED -> "${value.toInt()} tasks"
    WeeklyTrendType.QUIZ_SCORE      -> "${"%.0f".format(value)}% avg"
    WeeklyTrendType.REVIEW_COUNT    -> "${value.toInt()} reviews"
}
