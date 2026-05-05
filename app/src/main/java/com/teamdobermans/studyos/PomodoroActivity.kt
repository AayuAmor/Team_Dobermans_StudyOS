package com.teamdobermans.studyos

import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class PomodoroTab { FOCUS, SHORT_BREAK, LONG_BREAK }

class PomodoroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PomodoroBody() }
    }
}

@Composable
fun PomodoroBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    var selectedTab   by remember { mutableStateOf(PomodoroTab.FOCUS) }
    var focusMinutes  by remember { mutableStateOf(25f) }
    var shortMinutes  by remember { mutableStateOf(5f) }
    var longMinutes   by remember { mutableStateOf(15f) }
    var isRunning     by remember { mutableStateOf(false) }
    var sessionsToday by remember { mutableStateOf(0) }

    val totalSeconds: Int = when (selectedTab) {
        PomodoroTab.FOCUS       -> focusMinutes.toInt()  * 60
        PomodoroTab.SHORT_BREAK -> shortMinutes.toInt()  * 60
        PomodoroTab.LONG_BREAK  -> longMinutes.toInt()   * 60
    }

    var timeRemaining by remember(selectedTab, focusMinutes, shortMinutes, longMinutes) {
        mutableStateOf(totalSeconds)
    }

    LaunchedEffect(isRunning) {
        while (isRunning && timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
        if (timeRemaining == 0) {
            isRunning = false
            if (selectedTab == PomodoroTab.FOCUS) sessionsToday++
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) timeRemaining.toFloat() / totalSeconds else 1f,
        animationSpec = tween(500),
        label = "timerProgress"
    )

    val timeLabel = "%02d:%02d".format(timeRemaining / 60, timeRemaining % 60)
    val modeLabel = when (selectedTab) {
        PomodoroTab.FOCUS       -> "FOCUS"
        PomodoroTab.SHORT_BREAK -> "SHORT BREAK"
        PomodoroTab.LONG_BREAK  -> "LONG BREAK"
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { activity?.finish() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pomodoro Timer",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Notifications silenced during focus",
                color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
                .background(StudyPurpleLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(shape = RoundedCornerShape(50.dp), color = Color.White,
                modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    PomodoroTab.values().forEach { tab ->
                        val isSelected = tab == selectedTab
                        Surface(shape = RoundedCornerShape(50.dp),
                            color = if (isSelected) StudyPurple else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable {
                                selectedTab = tab; isRunning = false }) {
                            Text(
                                text = when (tab) {
                                    PomodoroTab.FOCUS       -> "Focus"
                                    PomodoroTab.SHORT_BREAK -> "Short Break"
                                    PomodoroTab.LONG_BREAK  -> "Long Break"
                                },
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFFEEEBFF), style = Stroke(width = 14.dp.toPx()))
                            drawArc(color = StudyPurple, startAngle = -90f,
                                sweepAngle = 360f * progress, useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(timeLabel, style = TextStyle(color = Color(0xFF1A1A2E),
                                fontSize = 40.sp, fontWeight = FontWeight.Bold))
                            Text(modeLabel, color = Color.Gray, fontSize = 12.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { isRunning = !isRunning },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)) {
                            Text(if (isRunning) "Pause" else "Start",
                                color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Button(onClick = { isRunning = false; timeRemaining = totalSeconds },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEB))) {
                            Text("Reset", color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customize durations", fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = Color(0xFF1A1A2E))
                    Spacer(modifier = Modifier.height(8.dp))
                    DurationSlider(
                        label = "Focus", value = focusMinutes, range = 5f..60f,
                        onValueChange = {
                            focusMinutes = it
                            if (selectedTab == PomodoroTab.FOCUS && !isRunning)
                                timeRemaining = it.toInt() * 60
                        }
                    )
                    DurationSlider(
                        label = "Short", value = shortMinutes, range = 1f..15f,
                        onValueChange = {
                            shortMinutes = it
                            if (selectedTab == PomodoroTab.SHORT_BREAK && !isRunning)
                                timeRemaining = it.toInt() * 60
                        }
                    )
                    DurationSlider(
                        label = "Long", value = longMinutes, range = 10f..30f,
                        onValueChange = {
                            longMinutes = it
                            if (selectedTab == PomodoroTab.LONG_BREAK && !isRunning)
                                timeRemaining = it.toInt() * 60
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sessions today", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$sessionsToday", color = StudyPurple, fontSize = 36.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

}


@Composable
fun DurationSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(44.dp), fontSize = 13.sp, color = Color.DarkGray)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = StudyPurple,
                activeTrackColor = StudyPurple,
                inactiveTrackColor = Color(0xFFD0CBFF)
            )
        )
        Text("${value.toInt()}m", modifier = Modifier.width(36.dp), fontSize = 13.sp,
            color = StudyPurple, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun PomodoroPreview() {
    PomodoroBody()
}