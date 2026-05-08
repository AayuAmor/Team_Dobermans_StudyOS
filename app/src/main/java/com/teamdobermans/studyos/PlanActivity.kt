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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class PlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PlanBody() }
    }
}

@Composable
fun PlanBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    val today        = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    val currentMonth = YearMonth.of(selectedDate.year, selectedDate.month)
    val dayHeaders   = listOf("M", "T", "W", "Th", "F", "S", "Su")
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysInMonth  = currentMonth.lengthOfMonth()

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { activity?.finish() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Plan", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()
        ) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                    Column(modifier = Modifier.width(80.dp)) {
                        Text(selectedDate.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()), color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault()), color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${selectedDate.dayOfMonth}", color = StudyPurple, fontWeight = FontWeight.ExtraBold, fontSize = 80.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            dayHeaders.forEach { day -> Text(day, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalCells = firstDayOfWeek - 1 + daysInMonth
                        val rows = (totalCells + 6) / 7
                        var dayCounter = 1
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - (firstDayOfWeek - 1) + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val date = currentMonth.atDay(dayNum)
                                        val isSelected = date == selectedDate
                                        val isToday    = date == today
                                        Box(
                                            modifier = Modifier.weight(1f).padding(2.dp).aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(when { isSelected -> StudyPurple; else -> Color.Transparent })
                                                .clickable { selectedDate = date },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$dayNum", fontSize = 11.sp, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when { isSelected -> Color.White; isToday -> StudyPurple; else -> Color(0xFF1A1A2E) })
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f).padding(2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanPreview() {
    StudyOSTheme { PlanBody() }
}