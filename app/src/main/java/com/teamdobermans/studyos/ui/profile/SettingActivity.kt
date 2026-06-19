package com.teamdobermans.studyos.ui.profile

import com.teamdobermans.studyos.R

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.teamdobermans.studyos.notification.StudyReminderScheduler
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.viewModel.SettingsViewModel
import kotlinx.coroutines.delay

class SettingsActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
                StudyReminderScheduler.createChannel(this)
        setContent { SettingsBody(viewModel = settingsViewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBody(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit = {},
    onNavigateProfile: () -> Unit = {}
) {
    val signedOut by viewModel.signedOut.collectAsState()
    val uiState   by viewModel.uiState.collectAsState()
    val context   = LocalContext.current

    LaunchedEffect(signedOut) {
        if (signedOut) onSignOut()
    }

        LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(3_000)
            viewModel.clearMessages()
        }
    }

        val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
                        viewModel.setRemindersEnabled(true)
        }
            }

        val onReminderToggle: (Boolean) -> Unit = { enabled ->
        if (!enabled) {
            viewModel.setRemindersEnabled(false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setRemindersEnabled(true)
        }
    }

        var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialHour   = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            onConfirm = { h, m ->
                viewModel.setReminderTime(h, m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

        var offlineMode by remember { mutableStateOf(true) }
    var focusSounds by remember { mutableStateOf(false) }
    var pinNotes    by remember { mutableStateOf(false) }

    var dailyGoalMin by remember { mutableStateOf(60) }
    var goalDialog   by remember { mutableStateOf(false) }
    var goalInput    by remember { mutableStateOf("") }

    if (goalDialog) {
        AlertDialog(
            onDismissRequest = { goalDialog = false; goalInput = "" },
            title = {
                Text("Daily Study Goal", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            },
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
                        focusedIndicatorColor   = StudyPurple
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = goalInput.toIntOrNull()
                        if (mins != null && mins > 0) dailyGoalMin = mins
                        goalDialog = false
                        goalInput  = ""
                    },
                    enabled = goalInput.toIntOrNull()?.let { it > 0 } == true,
                    colors  = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalDialog = false; goalInput = "" }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

        Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Settings",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
                .background(StudyPurpleLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

                        Card(
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "App Preferences",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleRow("Offline mode", offlineMode) { offlineMode = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

                    StudyRemindersRow(
                        checked        = uiState.remindersEnabled,
                        saving         = uiState.saving,
                        loading        = uiState.loading,
                        successMessage = uiState.successMessage,
                        errorMessage   = uiState.errorMessage,
                        onCheckedChange = onReminderToggle
                    )

                                        if (uiState.remindersEnabled) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        ReminderTimeRow(
                            hour    = uiState.reminderHour,
                            minute  = uiState.reminderMinute,
                            onClick = { showTimePicker = true }
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Focus Sounds", focusSounds) { focusSounds = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Pin Notes",    pinNotes)    { pinNotes    = it }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

                        Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { goalInput = dailyGoalMin.toString(); goalDialog = true },
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Daily Study Goal",
                            color = StudyPurple,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Icon(
                            painter = painterResource(R.drawable.baseline_more_horiz_24),
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$dailyGoalMin min / Day", color = Color(0xFF1A1A2E), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

                        Card(
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Export and Data",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDEEEFF)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Export Notes as PDF",
                                color = Color(0xFF1A62B7),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEEBFF)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp)
                                .clickable { viewModel.signOut() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign Out",
                                color = StudyPurple,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StudyRemindersRow(
    checked: Boolean,
    saving: Boolean,
    loading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Study Reminders",
                    color = Color(0xFF1A1A2E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Receive focus session and study reminder notifications",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (loading || saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = StudyPurple,
                    strokeWidth = 2.dp
                )
            } else {
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

        val statusText = when {
            saving               -> "Saving..."
            successMessage != null -> successMessage
            errorMessage != null   -> errorMessage
            else                 -> null
        }
        if (statusText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                statusText,
                fontSize = 11.sp,
                color = if (errorMessage != null && !saving) Color(0xFFE53935) else Color(0xFF388E3C)
            )
        }
    }
}

@Composable
private fun ReminderTimeRow(hour: Int, minute: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Reminder Time", color = Color(0xFF1A1A2E), fontSize = 14.sp)
            Text(
                "Daily at ${formatTime(hour, minute)}",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        TextButton(onClick = onClick) {
            Text("Change", color = StudyPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Reminder Time",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                colors  = ButtonDefaults.buttonColors(containerColor = StudyPurple)
            ) {
                Text("Set", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
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

private fun formatTime(hour: Int, minute: Int): String {
    val amPm        = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else      -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, amPm)
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudyPurpleLight)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "App Preferences",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsToggleRow("Offline mode", true) {}
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    StudyRemindersRow(
                        checked        = true,
                        saving         = false,
                        loading        = false,
                        successMessage = null,
                        errorMessage   = null,
                        onCheckedChange = {}
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    ReminderTimeRow(hour = 8, minute = 0, onClick = {})
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Focus Sounds", false) {}
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Pin Notes",    false) {}
                }
            }
        }
    }
}
