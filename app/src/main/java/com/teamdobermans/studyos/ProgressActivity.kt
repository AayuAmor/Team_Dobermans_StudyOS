package com.teamdobermans.studyos

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import kotlin.random.Random

data class SubjectProgress(val name: String, val percent: Float, val color: Color)

private fun heatColor(level: Int): Color = when (level) {
    0    -> Color(0xFFE8E4FF)
    1    -> Color(0xFFBDB5FF)
    2    -> Color(0xFF9589F5)
    3    -> Color(0xFF6C5DD3)
    else -> Color(0xFF4A3EB8)
}

class ProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ProgressBody() }
    }
}

@Composable
fun ProgressBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    val subjectProgressList = listOf(
        SubjectProgress("Science", 0.40f, StudyPurple),
        SubjectProgress("Social",  0.45f, Color(0xFFE53935)),
        SubjectProgress("Math",    0.75f, Color(0xFFBFA300))
    )

    val heatData    = remember { Array(4) { IntArray(36) { Random.nextInt(0, 5) } } }
    val dayLabels   = listOf("Sun", "Tue", "Thu", "Sat")
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep")

    Scaffold(
        bottomBar = { StudyOSBottomNav(currentRoute = NavRoute.PROGRESS, context = context) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudyPurple)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.clickable { activity?.finish() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = Color.White, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Progress",
                    style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    listOf("Study Hours", "Streak", "Performance").forEach {
                        Text("• $it", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
                    .background(StudyPurpleLight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), label = "Streak",     value = "14", unit = "days 🔥")
                    StatCard(modifier = Modifier.weight(1f), label = "Cards Done", value = "0")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), label = "Quiz average", value = "10")
                    StatCard(modifier = Modifier.weight(1f), label = "Focus min",    value = "50", unit = "min")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Subject Progress", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        if (subjectProgressList.isEmpty()) {
                            Text("No subjects tracked yet", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            subjectProgressList.forEach { subject ->
                                SubjectBar(name = subject.name, percent = subject.percent, color = subject.color)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.padding(start = 32.dp)) {
                            monthLabels.forEach { month ->
                                Text(month, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        dayLabels.forEachIndexed { rowIndex, dayLabel ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dayLabel, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.width(28.dp))
                                heatData[rowIndex].forEach { level ->
                                    Box(modifier = Modifier.size(9.dp).padding(1.dp)
                                        .clip(RoundedCornerShape(2.dp)).background(heatColor(level)))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Less", fontSize = 9.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            (0..4).forEach { lvl ->
                                Box(modifier = Modifier.size(9.dp).padding(1.dp)
                                    .clip(RoundedCornerShape(2.dp)).background(heatColor(lvl)))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("More", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SubjectBar(name: String, percent: Float, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(name, modifier = Modifier.width(70.dp), fontSize = 13.sp, color = Color.DarkGray)
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
            .background(Color.Gray.copy(alpha = 0.15f))) {
            Box(modifier = Modifier.fillMaxWidth(percent).height(8.dp)
                .clip(RoundedCornerShape(4.dp)).background(color))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("${(percent * 100).toInt()}%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, unit: String = "") {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = StudyPurple, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) Text(unit, color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressPreview() {
    StudyOSTheme { ProgressBody() }
}