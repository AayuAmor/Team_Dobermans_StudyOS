package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.ProgressViewModel

private fun heatColor(level: Int): Color = when (level) {
    0    -> HeatL0
    1    -> HeatL1
    2    -> HeatL2
    3    -> HeatL3
    else -> HeatL4
}

class ProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ProgressBody(viewModel = ProgressViewModel()) }
    }
}

@Composable
fun ProgressBody(viewModel: ProgressViewModel = ProgressViewModel()) {
    val subjectProgressList by viewModel.subjectProgress.collectAsState()
    val heatData    = viewModel.heatData
    val dayLabels   = listOf("Sun", "Tue", "Thu", "Sat")
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleDeep)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Progress", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                listOf("Study Hours", "Streak", "Performance").forEach {
                    Text("$it", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(StudyPurpleFaint)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = "Streak",     value = "14", unit = "days")
                StatCard(modifier = Modifier.weight(1f), label = "Cards Done", value = "0")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = "Quiz average", value = "10")
                StatCard(modifier = Modifier.weight(1f), label = "Focus min",    value = "50", unit = "min")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Subject Progress", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    if (subjectProgressList.isEmpty()) {
                        Text("No subjects tracked yet", color = TextHint, fontSize = 13.sp)
                    } else {
                        subjectProgressList.forEach { subject ->
                            SubjectBar(name = subject.name, percent = subject.percent, color = Color(subject.colorArgb))
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.padding(start = 32.dp)) {
                        monthLabels.forEach { month ->
                            Text(month, fontSize = 9.sp, color = TextHint, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    dayLabels.forEachIndexed { rowIndex, dayLabel ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dayLabel, fontSize = 9.sp, color = TextHint, modifier = Modifier.width(28.dp))
                            heatData[rowIndex].forEach { level ->
                                Box(modifier = Modifier.size(9.dp).padding(1.dp)
                                    .clip(RoundedCornerShape(2.dp)).background(heatColor(level)))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", fontSize = 9.sp, color = TextHint)
                        Spacer(modifier = Modifier.width(4.dp))
                        (0..4).forEach { lvl ->
                            Box(modifier = Modifier.size(9.dp).padding(1.dp)
                                .clip(RoundedCornerShape(2.dp)).background(heatColor(lvl)))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("More", fontSize = 9.sp, color = TextHint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SubjectBar(name: String, percent: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("${(percent * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(StudyPurpleLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, unit: String = "") {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                label,
                color         = TextHint,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(unit, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressPreview() {
    ProgressBody(viewModel = ProgressViewModel())
}
