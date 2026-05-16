package com.teamdobermans.studyos

import android.app.Activity
import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import androidx.compose.material3.Scaffold
import androidx.compose.ui.focus.focusModifier

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SettingsBody() }
    }
}

@Composable
fun SettingsBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    var offlineMode   by remember { mutableStateOf(true) }
    var weeklySummary by remember { mutableStateOf(true) }
    var focusSounds   by remember { mutableStateOf(false) }
    var pinNotes      by remember { mutableStateOf(false) }

    var dailyGoalMin  by remember { mutableStateOf(60) }
    var goalDialog    by remember { mutableStateOf(false) }
    var goalInput     by remember { mutableStateOf("") }

    if (goalDialog) {
        AlertDialog(
            onDismissRequest = { goalDialog = false; goalInput = "" },
            title = { Text("Daily Study Goal", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)) },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() }.take(3) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Minutes per day", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F3FF),
                        focusedContainerColor   = Color(0xFFF5F3FF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor   = StudyPurple,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { val mins = goalInput.toIntOrNull(); if (mins != null && mins > 0) dailyGoalMin = mins; goalDialog = false; goalInput = "" },
                    enabled = goalInput.toIntOrNull()?.let { it > 0 } == true,
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { goalDialog = false; goalInput = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        bottomBar = { StudyOSBottomNav(currentRoute = NavRoute.SETTINGS, context = context) }
    ) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize().background(StudyPurple)
            .padding(bottom = innerPadding.calculateBottomPadding())) {

            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { activity?.finish() }) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = Color.White, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Settings", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            }

            Column(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                    .verticalScroll(rememberScrollState()).padding(16.dp)
            ) {

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("App Preferences", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsToggleRow("Offline mode",                offlineMode)   { offlineMode   = it }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingsToggleRow("Weekly Summary Notification", weeklySummary) { weeklySummary = it }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingsToggleRow("Focus Sounds",                focusSounds)  { focusSounds   = it }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingsToggleRow("Pin Notes",                   pinNotes)     { pinNotes      = it }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth().clickable { goalInput = dailyGoalMin.toString(); goalDialog = true },
                    shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Study Goal", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$dailyGoalMin min / Day", color = Color(0xFF1A1A2E), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text("Export and Data", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDEEEFF)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable { /* TODO: export notes as PDF */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Export Notes as PDF", color = Color(0xFF1A62B7), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEEEBFF)
                        ) {

                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable {
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                },
                                contentAlignment = Alignment.Center
                            ){
                                Text("Sign Out", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF1A1A2E), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudyPurple, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)))
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme { SettingsBody() }
}