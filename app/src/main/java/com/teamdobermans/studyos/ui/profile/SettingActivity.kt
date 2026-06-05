package com.teamdobermans.studyos.ui.profile
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.viewModel.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SettingsBody(viewModel = SettingsViewModel()) }
    }
}

@Composable
fun SettingsBody(
    viewModel: SettingsViewModel = SettingsViewModel(),
    onSignOut: () -> Unit = {},
    onNavigateProfile: () -> Unit = {}
) {
    val signedOut by viewModel.signedOut.collectAsState()

    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

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
                        unfocusedContainerColor = Color(0xFFF5F3FF), focusedContainerColor = Color(0xFFF5F3FF),
                        unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { val mins = goalInput.toIntOrNull(); if (mins != null && mins > 0) dailyGoalMin = mins; goalDialog = false; goalInput = "" },
                    enabled = goalInput.toIntOrNull()?.let { it > 0 } == true,
                    colors  = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { goalDialog = false; goalInput = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                    SettingsToggleRow("Focus Sounds",                focusSounds)   { focusSounds   = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Pin Notes",                   pinNotes)      { pinNotes      = it }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable { goalInput = dailyGoalMin.toString(); goalDialog = true },
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
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

                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFDEEEFF)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Export Notes as PDF", color = Color(0xFF1A62B7), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFEEEBFF)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable { viewModel.signOut() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sign Out", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = StudyPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme { SettingsBody(viewModel = SettingsViewModel()) }
}

