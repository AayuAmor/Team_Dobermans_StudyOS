package com.teamdobermans.studyos.ui.focus
import com.teamdobermans.studyos.R

import com.teamdobermans.studyos.ui.theme.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.EditingSlider
import com.teamdobermans.studyos.model.PomodoroTab
import com.teamdobermans.studyos.viewModel.PomodoroViewModel

class PomodoroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val autoStart = intent.getBooleanExtra("auto_start", false)
        setContent { PomodoroBody(onBack = { finish() }, autoStart = autoStart) }
    }
}

@Composable
fun PomodoroBody(
    onBack: () -> Unit = {},
    autoStart: Boolean = false
) {
    val viewModel: PomodoroViewModel = viewModel()
    val selectedTab   by viewModel.selectedTab.collectAsState()
    val focusMinutes  by viewModel.focusMinutes.collectAsState()
    val shortMinutes  by viewModel.shortMinutes.collectAsState()
    val longMinutes   by viewModel.longMinutes.collectAsState()
    val isRunning     by viewModel.isRunning.collectAsState()

    LaunchedEffect(Unit) {
        if (autoStart && !isRunning) viewModel.toggleTimer()
    }
    val sessionsToday by viewModel.sessionsToday.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    var editingSlider by remember { mutableStateOf(EditingSlider.NONE) }
    var dialogInput   by remember { mutableStateOf("") }

    val totalSeconds: Long = when (selectedTab) {
        PomodoroTab.FOCUS       -> (focusMinutes * 60).toLong()
        PomodoroTab.SHORT_BREAK -> (shortMinutes * 60).toLong()
        PomodoroTab.LONG_BREAK  -> (longMinutes  * 60).toLong()
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

    if (editingSlider != EditingSlider.NONE) {
        val dialogTitle = when (editingSlider) {
            EditingSlider.FOCUS -> "Focus duration"
            EditingSlider.SHORT -> "Short break duration"
            EditingSlider.LONG  -> "Long break duration"
            EditingSlider.NONE  -> ""
        }
        val dialogRange = when (editingSlider) {
            EditingSlider.FOCUS -> 5..60
            EditingSlider.SHORT -> 1..15
            EditingSlider.LONG  -> 10..30
            EditingSlider.NONE  -> 1..120
        }
        val parsedInput = dialogInput.toIntOrNull()
        val isValid     = parsedInput != null && parsedInput in dialogRange

        AlertDialog(
            onDismissRequest = { editingSlider = EditingSlider.NONE },
            title = { Text(dialogTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("${dialogRange.first}-${dialogRange.last} minutes", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dialogInput,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() }.take(3)
                            dialogInput = filtered
                            val live = filtered.toIntOrNull()
                            if (live != null) {
                                val clamped = live.coerceIn(dialogRange.first, dialogRange.last).toFloat()
                                when (editingSlider) {
                                    EditingSlider.FOCUS -> viewModel.setFocusMinutes(clamped)
                                    EditingSlider.SHORT -> viewModel.setShortMinutes(clamped)
                                    EditingSlider.LONG  -> viewModel.setLongMinutes(clamped)
                                    EditingSlider.NONE  -> {}
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = dialogInput.isNotEmpty() && !isValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = {
                            Text(when (editingSlider) {
                                EditingSlider.FOCUS -> "e.g. 25"
                                EditingSlider.SHORT -> "e.g. 5"
                                EditingSlider.LONG  -> "e.g. 15"
                                EditingSlider.NONE  -> ""
                            })
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = StudyPurpleFaint,
                            focusedContainerColor   = StudyPurpleFaint,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = { editingSlider = EditingSlider.NONE }, enabled = isValid, colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)) {
                    Text("Set", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSlider = EditingSlider.NONE }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { onBack() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Pomodoro Timer", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Notifications silenced during focus", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = RoundedCornerShape(50.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    PomodoroTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Surface(
                            shape    = RoundedCornerShape(50.dp),
                            color    = if (isSelected) StudyPurple else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { viewModel.selectTab(tab) }
                        ) {
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

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = StudyPurpleLight, style = Stroke(width = 14.dp.toPx()))
                            drawArc(color = StudyPurple, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(timeLabel, style = TextStyle(color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
                            Text(modeLabel, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { viewModel.toggleTimer() }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)) {
                            Text(if (isRunning) "Pause" else "Start", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Button(onClick = { viewModel.resetTimer() }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(23.dp), colors = ButtonDefaults.buttonColors(containerColor = PriorityHighBg)) {
                            Text("Reset", color = PriorityHigh, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customize durations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    DurationSlider(
                        label = "Focus", value = focusMinutes, range = 5f..60f,
                        onValueChange = { viewModel.setFocusMinutes(it) },
                        onValueTap = { dialogInput = focusMinutes.toInt().toString(); editingSlider = EditingSlider.FOCUS }
                    )
                    DurationSlider(
                        label = "Short", value = shortMinutes, range = 1f..15f,
                        onValueChange = { viewModel.setShortMinutes(it) },
                        onValueTap = { dialogInput = shortMinutes.toInt().toString(); editingSlider = EditingSlider.SHORT }
                    )
                    DurationSlider(
                        label = "Long", value = longMinutes, range = 10f..30f,
                        onValueChange = { viewModel.setLongMinutes(it) },
                        onValueTap = { dialogInput = longMinutes.toInt().toString(); editingSlider = EditingSlider.LONG }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sessions today",
                        color        = TextSecondary,
                        fontSize     = 12.sp,
                        fontWeight   = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (index < sessionsToday) StudyPurple else StudyPurpleLight)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$sessionsToday of 4 sessions", color = TextHint, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DurationSlider(label: String, value: Float, onValueChange: (Float) -> Unit, range: ClosedFloatingPointRange<Float>, onValueTap: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(44.dp), fontSize = 13.sp, color = Color.DarkGray)
        Slider(
            value = value, onValueChange = onValueChange, valueRange = range, modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = StudyPurple, activeTrackColor = StudyPurple, inactiveTrackColor = StudyPurpleLight)
        )
        Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight, modifier = Modifier.width(36.dp).clickable { onValueTap() }) {
            Text("${value.toInt()}m", modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp), fontSize = 13.sp, color = StudyPurple, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PomodoroPreview() {
    PomodoroBody(onBack = {})
}

