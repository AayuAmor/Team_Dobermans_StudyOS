package com.teamdobermans.studyos.ui.study
import com.teamdobermans.studyos.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.viewModel.MockTestViewModel

class MockTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MockTestBody(viewModel = MockTestViewModel(), onBack = { finish() })
        }
    }
}

@Composable
fun MockTestBody(
    viewModel: MockTestViewModel = MockTestViewModel(),
    onBack: () -> Unit = {}
) {
    val subjects         by viewModel.subjects.collectAsState()
    val selectedSubject  by viewModel.selectedSubject.collectAsState()
    val durationIndex    by viewModel.durationIndex.collectAsState()
    val questionIndex    by viewModel.questionIndex.collectAsState()
    val difficulty       by viewModel.difficulty.collectAsState()

    val durationSteps = listOf(10, 30, 60, 90, 120)
    val questionSteps = listOf(5, 15, 25, 35, 50)

    var subjectDropdown  by remember { mutableStateOf(false) }
    var addSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectInput  by remember { mutableStateOf("") }

    if (addSubjectDialog) {
        AlertDialog(
            onDismissRequest = { addSubjectDialog = false; newSubjectInput = "" },
            title = { Text("Add Subject", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)) },
            text = {
                OutlinedTextField(
                    value = newSubjectInput,
                    onValueChange = { newSubjectInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. Biology, Physics", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F3FF), focusedContainerColor = Color(0xFFF5F3FF),
                        unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addSubject(newSubjectInput.trim()); viewModel.selectSubject(newSubjectInput.trim()); addSubjectDialog = false; newSubjectInput = "" },
                    enabled = newSubjectInput.trim().isNotEmpty(),
                    colors  = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { addSubjectDialog = false; newSubjectInput = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { onBack() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Mock Test", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Timed exam simulation", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mock Test", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box {
                        Surface(shape = RoundedCornerShape(10.dp), color = StudyPurple, modifier = Modifier.clickable { subjectDropdown = true }) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedSubject, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject, fontWeight = if (subject == selectedSubject) FontWeight.Bold else FontWeight.Normal, color = if (subject == selectedSubject) StudyPurple else Color(0xFF1A1A2E)) },
                                    onClick = { viewModel.selectSubject(subject); subjectDropdown = false }
                                )
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painter = painterResource(R.drawable.baseline_notifications_none_24), contentDescription = null, tint = StudyPurple, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add new subject...", color = StudyPurple, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = { subjectDropdown = false; addSubjectDialog = true }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Duration", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${durationSteps[durationIndex]}", color = Color(0xFF1A1A2E), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("min", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    }
                    Slider(
                        value = durationIndex.toFloat(), onValueChange = { viewModel.setDurationIndex(it.toInt()) },
                        valueRange = 0f..4f, steps = 3, modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = StudyPurple, activeTrackColor = StudyPurple, inactiveTrackColor = Color(0xFFD0CBFF))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        durationSteps.forEach { step -> Text("${step}min", fontSize = 11.sp, color = Color.Gray) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Questions", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${questionSteps[questionIndex]}", color = Color(0xFF1A1A2E), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("questions", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    }
                    Slider(
                        value = questionIndex.toFloat(), onValueChange = { viewModel.setQuestionIndex(it.toInt()) },
                        valueRange = 0f..4f, steps = 3, modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = StudyPurple, activeTrackColor = StudyPurple, inactiveTrackColor = Color(0xFFD0CBFF))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        questionSteps.forEach { step -> Text("$step", fontSize = 11.sp, color = Color.Gray) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("DIFFICULTY", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DifficultyChip(label = "Easy",   selected = difficulty == Difficulty.EASY,   selectedColor = Color(0xFF4CAF50), bgColor = Color(0xFFE8F5E9), modifier = Modifier.weight(1f)) { viewModel.setDifficulty(Difficulty.EASY) }
                DifficultyChip(label = "Medium", selected = difficulty == Difficulty.MEDIUM, selectedColor = Color(0xFFFFC107), bgColor = Color(0xFFFFF8E1), modifier = Modifier.weight(1f)) { viewModel.setDifficulty(Difficulty.MEDIUM) }
                DifficultyChip(label = "Hard",   selected = difficulty == Difficulty.HARD,   selectedColor = Color(0xFFE53935), bgColor = Color(0xFFFFEBEB), modifier = Modifier.weight(1f)) { viewModel.setDifficulty(Difficulty.HARD) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = {},
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
            ) { Text("Start Test", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DifficultyChip(label: String, selected: Boolean, selectedColor: Color, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(50.dp), color = bgColor, modifier = modifier.clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) selectedColor else Color.Gray, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MockTestPreview() {
    MockTestBody(viewModel = MockTestViewModel(), onBack = {})
}

