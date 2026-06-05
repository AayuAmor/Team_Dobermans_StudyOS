package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.VisionBoardViewModel

class VisionBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VisionBoardBody(viewModel = VisionBoardViewModel(), onBack = { finish() }) }
    }
}

@Composable
fun VisionBoardBody(
    viewModel: VisionBoardViewModel = VisionBoardViewModel(),
    onBack: () -> Unit = {}
) {
    val goalText        by viewModel.goalText.collectAsState()
    val selectedEmoji   by viewModel.selectedEmoji.collectAsState()
    val targetValue     by viewModel.targetValue.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val pinnedGoals     by viewModel.pinnedGoals.collectAsState()

    val subjects   = listOf("General", "Biology", "Physics", "Math")
    val emojiOptions = listOf("🏅", "⭐", "💪", "🚀", "💵")
    val canPin = goalText.trim().isNotEmpty()

    var subjectDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple).imePadding()) {

        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Vision Board", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Goals - Images - Subject targets", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp))
                .background(StudyPurpleFaint)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {

            Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add goal", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { viewModel.setGoalText(it) },
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("Your goal or dream...", color = TextHint) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = StudyPurple,
                            unfocusedBorderColor    = StudyPurpleLight,
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor             = StudyPurple,
                            focusedTextColor        = TextPrimary,
                            unfocusedTextColor      = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojiOptions.forEach { emoji ->
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (emoji == selectedEmoji) StudyPurple.copy(alpha = 0.15f) else StudyPurpleFaint,
                                modifier = Modifier.size(44.dp).clickable { viewModel.setEmoji(emoji) }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Subject", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = StudyPurpleLight,
                            modifier = Modifier.fillMaxWidth().clickable { subjectDropdown = true }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(selectedSubject, color = StudyPurple, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = StudyPurple, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = {
                                        Text(subject,
                                            fontWeight = if (subject == selectedSubject) FontWeight.Bold else FontWeight.Normal,
                                            color = if (subject == selectedSubject) StudyPurple else TextPrimary)
                                    },
                                    onClick = { viewModel.selectSubject(subject); subjectDropdown = false }
                                )
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            DropdownMenuItem(text = { Text("Add subject...", color = StudyPurple, fontWeight = FontWeight.SemiBold) }, onClick = { subjectDropdown = false })
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Target value", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { viewModel.setTargetValue(it.filter { c -> c.isDigit() || c == '%' }.take(10)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        placeholder = { Text("e.g. 100 pages, 90%", color = TextHint) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = StudyPurple,
                            unfocusedBorderColor    = StudyPurple.copy(alpha = 0.4f),
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor             = StudyPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick  = { viewModel.pinGoal() },
                        enabled  = canPin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(25.dp)),
                        shape  = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                    ) {
                        Text("Pin to Board", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pinnedGoals.isNotEmpty()) {
                val rows = pinnedGoals.chunked(2)
                rows.forEach { rowGoals ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGoals.forEach { goal ->
                            Card(modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Text(goal.emoji, fontSize = 30.sp)
                                        Text("x", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.clickable { viewModel.removeGoal(goal) })
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(goal.text, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.1.sp)
                                    if (goal.targetValue.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Target: ${goal.targetValue}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        if (rowGoals.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VisionBoardPreview() {
    StudyOSTheme { VisionBoardBody(viewModel = VisionBoardViewModel(), onBack = {}) }
}
