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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.UserPreferences
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.viewModel.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                SettingsBody(
                    onSignOut = { finish() },
                    onNavigateProfile = { finish() }
                )
            }
        }
    }
}

@Composable
fun SettingsBody(
    viewModel: SettingsViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    onNavigateProfile: () -> Unit = {}
) {
    val prefs by viewModel.prefs.collectAsState()
    val signedOut by viewModel.signedOut.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Navigate away when signed out
    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

    SettingsBodyContent(
        prefs = prefs,
        isSaving = isSaving,
        onSave = { updatedPrefs -> viewModel.savePreferences(updatedPrefs) },
        onSignOutClick = { viewModel.signOut() },
        onBack = onNavigateProfile
    )
}

@Composable
fun SettingsBodyContent(
    prefs: UserPreferences,
    isSaving: Boolean,
    onSave: (UserPreferences) -> Unit,
    onSignOutClick: () -> Unit,
    onBack: () -> Unit
) {
    // Local copies for editing before saving
    var offlineMode   by remember(prefs) { mutableStateOf(prefs.offlineMode) }
    var weeklySummary by remember(prefs) { mutableStateOf(prefs.weeklySummary) }
    var focusSounds   by remember(prefs) { mutableStateOf(prefs.focusSounds) }
    var pinNotes      by remember(prefs) { mutableStateOf(prefs.pinNotes) }
    var dailyGoalMin  by remember(prefs) { mutableIntStateOf(prefs.dailyGoalMin) }
    var pomodoroDuration by remember(prefs) { mutableIntStateOf(prefs.pomodoroDuration) }

    var goalDialog  by remember { mutableStateOf(false) }
    var goalInput   by remember { mutableStateOf("") }
    var pomodoroDialog by remember { mutableStateOf(false) }
    var pomodoroInput  by remember { mutableStateOf("") }

    // ── Daily Goal Dialog ──────────────────────────────────────────────
    if (goalDialog) {
        AlertDialog(
            onDismissRequest = { goalDialog = false },
            title = { Text("Daily Study Goal", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { c -> c.isDigit() }.take(3) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Minutes per day") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        goalInput.toIntOrNull()?.let { if (it > 0) dailyGoalMin = it }
                        goalDialog = false
                    },
                    enabled = goalInput.toIntOrNull()?.let { it > 0 } == true,
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { goalDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Pomodoro Dialog ────────────────────────────────────────────────
    if (pomodoroDialog) {
        AlertDialog(
            onDismissRequest = { pomodoroDialog = false },
            title = { Text("Pomodoro Duration", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pomodoroInput,
                    onValueChange = { pomodoroInput = it.filter { c -> c.isDigit() }.take(2) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Minutes (e.g. 25)") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pomodoroInput.toIntOrNull()?.let { if (it > 0) pomodoroDuration = it }
                        pomodoroDialog = false
                    },
                    enabled = pomodoroInput.toIntOrNull()?.let { it > 0 } == true,
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { pomodoroDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        // ── Header ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Settings",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }

        // ── Body ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(StudyPurpleLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // App Preferences card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Preferences", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsToggleRow("Offline Mode", offlineMode) { offlineMode = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Weekly Summary Notification", weeklySummary) { weeklySummary = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Focus Sounds", focusSounds) { focusSounds = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Pin Notes", pinNotes) { pinNotes = it }
                }
            }

            // Daily Goal card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { goalInput = dailyGoalMin.toString(); goalDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Study Goal", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$dailyGoalMin min / day", color = Color(0xFF1A1A2E), fontSize = 14.sp)
                    }
                    Icon(painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray)
                }
            }

            // Pomodoro Duration card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pomodoroInput = pomodoroDuration.toString(); pomodoroDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pomodoro Duration", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$pomodoroDuration min", color = Color(0xFF1A1A2E), fontSize = 14.sp)
                    }
                    Icon(painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray)
                }
            }

            // Save button
            Button(
                onClick = {
                    onSave(
                        UserPreferences(
                            offlineMode = offlineMode,
                            weeklySummary = weeklySummary,
                            focusSounds = focusSounds,
                            pinNotes = pinNotes,
                            dailyGoalMin = dailyGoalMin,
                            pomodoroDuration = pomodoroDuration
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = StudyPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Settings", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Export and Sign Out card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Export and Data", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDEEEFF)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Export Notes as PDF", color = Color(0xFF1A62B7), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEEBFF)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp).clickable { onSignOutClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sign Out", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudyPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme {
        SettingsBodyContent(
            prefs = UserPreferences(),
            isSaving = false,
            onSave = {},
            onSignOutClick = {},
            onBack = {}
        )
    }
}