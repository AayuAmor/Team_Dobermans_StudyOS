package com.teamdobermans.studyos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AnalyticsUiState

@Composable
fun AnalyticsHeroRow(state: AnalyticsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnalyticsMetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Rounded.Whatshot,
            iconColor = Color(0xFFE04F7B),
            value     = "${state.studyStreak}",
            unit      = "days",
            label     = "Streak"
        )
        AnalyticsMetricCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Rounded.Schedule,
            iconColor = Color(0xFF3A82E0),
            value     = "${state.monthlyHours.toInt()}",
            unit      = "hrs",
            label     = "This Month"
        )
        AnalyticsMetricCard(
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
fun AnalyticsMetricCard(
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
