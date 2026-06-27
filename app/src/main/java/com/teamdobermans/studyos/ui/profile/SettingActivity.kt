package com.teamdobermans.studyos.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.ui.components.StudyOSTextButton
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.SettingsUiState
import com.teamdobermans.studyos.viewModel.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { StudyOSTheme { SettingsBody(viewModel = settingsViewModel, onSignOut = { finish() }) } }
    }
}

@Composable
fun SettingsBody(
    viewModel: SettingsViewModel = viewModel(),
    onSignOut: () -> Unit = {},
    onNavigateProfile: () -> Unit = {}
) {
    val signedOut by viewModel.signedOut.collectAsState()
    val state by viewModel.state.collectAsState()
    var timeDialog by remember { mutableStateOf(false) }
    var hourInput by remember { mutableStateOf(state.reminderHour.toString()) }
    var minuteInput by remember { mutableStateOf(state.reminderMinute.toString().padStart(2, '0')) }

    LaunchedEffect(signedOut) { if (signedOut) onSignOut() }
    LaunchedEffect(state.reminderHour, state.reminderMinute) {
        hourInput = state.reminderHour.toString()
        minuteInput = state.reminderMinute.toString().padStart(2, '0')
    }

    if (timeDialog) {
        AlertDialog(
            onDismissRequest = { timeDialog = false },
            title = { Text("Reminder time", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hourInput,
                        onValueChange = { hourInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minuteInput,
                        onValueChange = { minuteInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                StudyOSTextButton(text = "Save", onClick = {
                    viewModel.setReminderTime(hourInput.toIntOrNull() ?: 8, minuteInput.toIntOrNull() ?: 0)
                    timeDialog = false
                })
            },
            dismissButton = { StudyOSTextButton(text = "Cancel", onClick = { timeDialog = false }) }
        )
    }

    SettingsContent(
        state = state,
        onReminderToggle = viewModel::setRemindersEnabled,
        onReminderTimeClick = { timeDialog = true },
        onProfileClick = onNavigateProfile,
        onSignOut = viewModel::signOut
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Column {
                Text("Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Account, reminders, and study preferences",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsSection("Profile / account") {
                SettingsRow(
                    title = state.userName,
                    subtitle = state.userEmail.ifBlank { "Signed in account" },
                    onClick = onProfileClick
                )
            }

            SettingsSection("Reminder settings") {
                SettingsToggleRow(
                    "Daily study reminders",
                    "Syncs locally and to Firestore",
                    state.remindersEnabled,
                    onReminderToggle
                )
                HorizontalDivider(color = StudyPurpleLight)
                SettingsRow(
                    title = "Reminder time",
                    subtitle = "%02d:%02d".format(state.reminderHour, state.reminderMinute),
                    onClick = onReminderTimeClick
                )
            }

            SettingsSection("Study preferences") {
                SettingsInfoRow("Focus sounds", "Managed from the Focus screen")
                HorizontalDivider(color = StudyPurpleLight)
                SettingsInfoRow("Offline mode", "Uses cached Firebase/local data where available")
            }

            SettingsSection("Data / export") {
                SettingsInfoRow("Export notes", "Coming soon")
                HorizontalDivider(color = StudyPurpleLight)
                SettingsInfoRow("Cloud sync", "Notes, goals, quizzes, and flashcards sync with Firestore")
            }

            SettingsSection("Account actions") {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSignOut() },
                    color = StudyPurpleLight
                ) {
                    Text(
                        "Sign Out",
                        color = StudyPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onClick() }
        .padding(vertical = 10.dp)) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsInfoRow(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun SettingsToggleRow(label: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudyPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StudyPurpleLight
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme {
        SettingsContent(
            state = SettingsUiState(isLoading = false, userEmail = "learner@example.com"),
            onReminderToggle = {},
            onReminderTimeClick = {},
            onProfileClick = {},
            onSignOut = {}
        )
    }
}
