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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple

import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight

data class PinnedGoal(val emoji: String, val text: String, val target: String)

class VisionBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VisionBoardBody() }
    }
}

@Composable
fun VisionBoardBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    var goalText        by remember { mutableStateOf("") }
    var selectedEmoji   by remember { mutableStateOf("🏅") }
    var targetValue     by remember { mutableStateOf("") }
    var subjectDropdown by remember { mutableStateOf(false) }
    val subjects        = remember { mutableStateListOf("General", "Biology", "Physics", "Math") }
    var selectedSubject by remember { mutableStateOf("General") }
    val pinnedGoals     = remember { mutableStateListOf(PinnedGoal("🏅", "Get 90% in finals", "90%"), PinnedGoal("📚", "Study 2 hrs daily", "")) }

    val emojiOptions = listOf("🏅", "⭐", "💪", "🚀", "💵")
    val canPin       = goalText.trim().isNotEmpty()

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
            Text("Vision Board", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Goals • Images • Subject targets", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()
        ) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Add goal", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("Your goal or dream...", color = Color.White.copy(alpha = 0.7f)) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = StudyPurple,
                            focusedContainerColor   = StudyPurple,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedTextColor      = Color.White,
                            focusedTextColor        = Color.White,
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojiOptions.forEach { emoji ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (emoji == selectedEmoji) StudyPurple.copy(alpha = 0.15f) else Color(0xFFF5F3FF),
                                modifier = Modifier.size(44.dp).clickable { selectedEmoji = emoji }
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Subject-level goal", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF0EEFF), modifier = Modifier.fillMaxWidth().clickable { subjectDropdown = true }) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(selectedSubject, color = StudyPurple, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = StudyPurple, modifier = Modifier.size(16.dp))
                                }
                            }
                            DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                                subjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject, fontWeight = if (subject == selectedSubject) FontWeight.Bold else FontWeight.Normal, color = if (subject == selectedSubject) StudyPurple else Color(0xFF1A1A2E)) },
                                        onClick = { selectedSubject = subject; subjectDropdown = false }
                                    )
                                }
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                                DropdownMenuItem(text = { Text("Add subject...", color = StudyPurple, fontWeight = FontWeight.SemiBold) }, onClick = { subjectDropdown = false })
                            }
                        }
                        OutlinedTextField(
                            value = targetValue,
                            onValueChange = { targetValue = it.filter { c -> c.isDigit() || c == '%' }.take(5) },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            placeholder = { Text("Target", color = Color.Gray, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF0EEFF),
                                focusedContainerColor   = Color(0xFFF0EEFF),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor   = StudyPurple,
                            )
                        )
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = StudyPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Target", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (canPin) Color(0xFFEEEBFF) else Color(0xFFF5F5F5)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable(enabled = canPin) {
                                pinnedGoals.add(PinnedGoal(selectedEmoji, goalText.trim(), targetValue.trim()))
                                goalText = ""
                                targetValue = ""
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pin to Board", color = if (canPin) StudyPurple else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pinnedGoals.isNotEmpty()) {
                val rows = pinnedGoals.chunked(2)
                rows.forEach { rowGoals ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGoals.forEach { goal ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Text(goal.emoji, fontSize = 30.sp)
                                        Text("×", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.clickable { pinnedGoals.remove(goal) })
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(goal.text, color = Color(0xFF1A1A2E), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    if (goal.target.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Target: ${goal.target}", color = Color.Gray, fontSize = 11.sp)
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

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
//        StudyBottomNav(selected = 0)
//    }
}

@Preview(showBackground = true)
@Composable
fun VisionBoardPreview() {
    StudyOSTheme { VisionBoardBody() }
}