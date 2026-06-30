package com.teamdobermans.studyos.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.ui.auth.LoginActivity
import com.teamdobermans.studyos.ui.components.StudyOSDestructiveButton
import com.teamdobermans.studyos.ui.components.StudyOSOutlinedButton
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.components.StudyOSTextButton
import com.teamdobermans.studyos.ui.theme.PriorityHigh
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyCardBg
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextHint
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.SettingsUiState
import com.teamdobermans.studyos.viewModel.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                SettingsBody(
                    viewModel = settingsViewModel,
                    onSignOut = {
                        startActivity(
                            Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                    }
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
    val context = LocalContext.current
    val signedOut by viewModel.signedOut.collectAsState()
    val state by viewModel.state.collectAsState()

    var profileDialog by remember { mutableStateOf(false) }
    var reminderTimeDialog by remember { mutableStateOf(false) }
    var dailyGoalDialog by remember { mutableStateOf(false) }
    var focusDialog by remember { mutableStateOf(false) }
    var signOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(signedOut) { if (signedOut) onSignOut() }
    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    if (profileDialog) {
        UserProfileDialog(
            state = state,
            onDismiss = { profileDialog = false },
            onSaveName = { name ->
                when {
                    state.isLoading -> Toast.makeText(
                        context,
                        "Please wait, your request is processing",
                        Toast.LENGTH_SHORT
                    ).show()

                    name.isBlank() -> Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                    name.trim() == state.userName -> Toast.makeText(context, "No changes to save", Toast.LENGTH_SHORT)
                        .show()

                    !state.isAuthenticated -> Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                    else -> viewModel.updateDisplayName(name.trim())
                }
            }
        )
    }

    if (reminderTimeDialog) {
        NumberPairDialog(
            title = "Reminder time",
            firstLabel = "Hour",
            secondLabel = "Minute",
            firstInitial = state.reminderHour,
            secondInitial = state.reminderMinute,
            firstRange = 0..23,
            secondRange = 0..59,
            onDismiss = { reminderTimeDialog = false },
            onSave = { hour, minute ->
                viewModel.setReminderTime(hour, minute)
                Toast.makeText(context, "Reminder time saved", Toast.LENGTH_SHORT).show()
                reminderTimeDialog = false
            }
        )
    }

    if (dailyGoalDialog) {
        SingleNumberDialog(
            title = "Daily study goal",
            label = "Minutes per day",
            initial = state.dailyStudyGoalMinutes,
            range = 15..720,
            onDismiss = { dailyGoalDialog = false },
            onSave = { minutes ->
                viewModel.setDailyStudyGoalMinutes(minutes)
                Toast.makeText(context, "Daily study goal saved", Toast.LENGTH_SHORT).show()
                dailyGoalDialog = false
            }
        )
    }

    if (focusDialog) {
        NumberPairDialog(
            title = "Default focus session",
            firstLabel = "Focus minutes",
            secondLabel = "Break minutes",
            firstInitial = state.focusDurationMinutes,
            secondInitial = state.breakDurationMinutes,
            firstRange = 5..120,
            secondRange = 1..60,
            onDismiss = { focusDialog = false },
            onSave = { focus, breakMinutes ->
                viewModel.setFocusDurationMinutes(focus)
                viewModel.setBreakDurationMinutes(breakMinutes)
                Toast.makeText(context, "Focus defaults saved", Toast.LENGTH_SHORT).show()
                focusDialog = false
            }
        )
    }

    if (signOutDialog) {
        AlertDialog(
            onDismissRequest = { signOutDialog = false },
            title = { Text("Sign out?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("You will need to sign in again to sync your StudyOS data.", color = TextSecondary) },
            confirmButton = {
                StudyOSDestructiveButton(
                    text = "Sign Out",
                    onClick = {
                        signOutDialog = false
                        viewModel.signOut()
                    }
                )
            },
            dismissButton = { StudyOSTextButton(text = "Cancel", onClick = { signOutDialog = false }) },
            containerColor = StudyCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    val notificationPermissionGranted = rememberHasNotificationPermission()

    SettingsContent(
        state = state,
        appVersion = rememberAppVersion(),
        onManageProfile = { profileDialog = true },
        onReminderToggle = { enabled ->
            when {
                !state.isAuthenticated -> Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                enabled && !notificationPermissionGranted -> Toast.makeText(
                    context,
                    "Please allow notifications to enable reminders",
                    Toast.LENGTH_SHORT
                ).show()

                else -> viewModel.setRemindersEnabled(enabled)
            }
        },
        onReminderTimeClick = {
            if (!state.remindersEnabled) Toast.makeText(context, "Turn on study reminders first", Toast.LENGTH_SHORT)
                .show()
            else reminderTimeDialog = true
        },
        onDailyGoalClick = { dailyGoalDialog = true },
        onFocusDefaultsClick = { focusDialog = true },
        onSignOutClick = {
            if (!state.isAuthenticated) Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
            else signOutDialog = true
        }
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    appVersion: String,
    onManageProfile: () -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeClick: () -> Unit,
    onDailyGoalClick: () -> Unit,
    onFocusDefaultsClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        SettingsHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                LoadingSettingsCard()
            }

            SettingsSection(title = "Account") {
                AccountRow(state = state, onClick = onManageProfile)
                SectionDivider()
                SettingsRow(
                    icon = if (state.isAuthenticated) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    title = "Cloud sync",
                    subtitle = if (state.isAuthenticated) "Signed in and syncing" else "Sign in to sync your data",
                    trailingText = null,
                    onClick = null
                )
            }

            SettingsSection(title = "Notifications & Reminders") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Daily study reminder",
                    subtitle = "Get a reminder to keep your study routine consistent.",
                    checked = state.remindersEnabled,
                    onCheckedChange = onReminderToggle
                )
                SectionDivider()
                SettingsRow(
                    icon = Icons.Rounded.AccessTime,
                    title = "Reminder time",
                    subtitle = "Daily reminder schedule",
                    trailingText = "%02d:%02d".format(state.reminderHour, state.reminderMinute),
                    onClick = onReminderTimeClick
                )
            }

            SettingsSection(title = "Study Preferences") {
                SettingsRow(
                    icon = Icons.Rounded.Schedule,
                    title = "Daily study goal",
                    subtitle = "Used for progress planning.",
                    trailingText = formatMinutesAsHours(state.dailyStudyGoalMinutes),
                    onClick = onDailyGoalClick
                )
                SectionDivider()
                SettingsRow(
                    icon = Icons.Rounded.Timer,
                    title = "Default focus session",
                    subtitle = "Pomodoro defaults for focus planning.",
                    trailingText = "${state.focusDurationMinutes} min · ${state.breakDurationMinutes} min break",
                    onClick = onFocusDefaultsClick
                )
            }

            SettingsSection(title = "Data & Storage") {
                SettingsRow(
                    icon = Icons.Rounded.Storage,
                    title = "Sync notes and flashcards",
                    subtitle = if (state.isAuthenticated) "Your study data syncs with your signed-in account." else "Sign in to sync notes and flashcards.",
                    trailingText = null,
                    onClick = null
                )
            }

            SettingsSection(title = "Privacy & Security") {
                SettingsRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy note",
                    subtitle = "Your notes and study data are linked to your signed-in account.",
                    trailingText = null,
                    onClick = null
                )
            }

            SettingsSection(title = "About") {
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = "App version",
                    subtitle = appVersion,
                    trailingText = null,
                    onClick = null
                )
                SectionDivider()
                SettingsRow(
                    icon = Icons.Rounded.Badge,
                    title = "Made by",
                    subtitle = "Team Dobermans",
                    trailingText = null,
                    onClick = null
                )
            }

            SettingsSection(title = "Danger Zone") {
                DangerRow(onClick = onSignOutClick)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp)
    ) {
        Column {
            Text("Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Manage your account, reminders, and study preferences.",
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyCardBg),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AccountRow(state: SettingsUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InitialsAvatar(name = state.userName, email = state.userEmail)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                state.userName,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                accountSubtitle(state),
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text("Manage profile", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String?,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(trailingText, color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudyPurple,
                uncheckedThumbColor = StudyPurple,
                uncheckedTrackColor = StudyPurpleLight,
                uncheckedBorderColor = StudyPurpleLight
            )
        )
    }
}

@Composable
private fun DangerRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = Icons.AutoMirrored.Rounded.Logout, tint = PriorityHigh, background = Color(0xFFFFF0F0))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Sign out", color = PriorityHigh, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Sign out from this device.", color = TextSecondary, fontSize = 12.sp)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = PriorityHigh,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun IconTile(icon: ImageVector, tint: Color = StudyPurple, background: Color = StudyPurpleLight) {
    Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun InitialsAvatar(name: String, email: String) {
    val initials = remember(name, email) {
        val source = name.takeIf { it.isNotBlank() } ?: email.substringBefore("@").ifBlank { "Student" }
        source.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "ST" }
    }
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(StudyPurple),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun UserProfileDialog(state: SettingsUiState, onDismiss: () -> Unit, onSaveName: (String) -> Unit) {
    var editName by remember(state.userName) { mutableStateOf(state.userName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("User Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = state.userName, email = state.userEmail)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(state.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(accountSubtitle(state), color = TextSecondary, fontSize = 12.sp)
                    }
                }
                ProfileDetailRow(
                    "Email",
                    state.userEmail.ifBlank { if (state.isAuthenticated) "Signed in user" else "Sign in required" })
                ProfileDetailRow("Provider", state.providerName)
                ProfileDetailRow(
                    "Account status",
                    if (state.isAuthenticated) "Signed in and syncing" else "Sign in required"
                )
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Display name") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = StudyPurple) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudyPurple,
                        unfocusedBorderColor = StudyPurpleLight,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = StudyPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            StudyOSPrimaryButton(
                text = if (state.isLoading) "Saving..." else "Save",
                isLoading = state.isLoading,
                onClick = { onSaveName(editName) }
            )
        },
        dismissButton = { StudyOSOutlinedButton(text = "Close", onClick = onDismiss) },
        containerColor = StudyCardBg,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Column {
        Text(label, color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SingleNumberDialog(
    title: String,
    label: String,
    initial: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val context = LocalContext.current
    var value by remember(initial) { mutableStateOf(initial.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter(Char::isDigit).take(3) },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudyPurple, cursorColor = StudyPurple),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            StudyOSPrimaryButton(text = "Save", onClick = {
                val parsed = value.toIntOrNull()
                if (parsed == null || parsed !in range) {
                    Toast.makeText(
                        context,
                        "Enter a value between ${range.first} and ${range.last}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@StudyOSPrimaryButton
                }
                onSave(parsed)
            })
        },
        dismissButton = { StudyOSTextButton(text = "Cancel", onClick = onDismiss) },
        containerColor = StudyCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun NumberPairDialog(
    title: String,
    firstLabel: String,
    secondLabel: String,
    firstInitial: Int,
    secondInitial: Int,
    firstRange: IntRange,
    secondRange: IntRange,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var first by remember(firstInitial) { mutableStateOf(firstInitial.toString()) }
    var second by remember(secondInitial) { mutableStateOf(secondInitial.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = first,
                    onValueChange = { first = it.filter(Char::isDigit).take(3) },
                    label = { Text(firstLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudyPurple,
                        cursorColor = StudyPurple
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = second,
                    onValueChange = { second = it.filter(Char::isDigit).take(3) },
                    label = { Text(secondLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudyPurple,
                        cursorColor = StudyPurple
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            StudyOSPrimaryButton(text = "Save", onClick = {
                val parsedFirst = first.toIntOrNull()
                val parsedSecond = second.toIntOrNull()
                if (parsedFirst == null || parsedFirst !in firstRange) {
                    Toast.makeText(
                        context,
                        "$firstLabel must be ${firstRange.first}-${firstRange.last}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@StudyOSPrimaryButton
                }
                if (parsedSecond == null || parsedSecond !in secondRange) {
                    Toast.makeText(
                        context,
                        "$secondLabel must be ${secondRange.first}-${secondRange.last}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@StudyOSPrimaryButton
                }
                onSave(parsedFirst, parsedSecond)
            })
        },
        dismissButton = { StudyOSTextButton(text = "Cancel", onClick = onDismiss) },
        containerColor = StudyCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LoadingSettingsCard() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = StudyCardBg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = StudyPurple, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Loading settings…", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = StudyPurpleLight, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Installed version"
        }.getOrDefault("Installed version")
    }
}

@Composable
private fun rememberHasNotificationPermission(): Boolean {
    val context = LocalContext.current
    return remember {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun accountSubtitle(state: SettingsUiState): String = when {
    !state.isAuthenticated -> "Sign in required"
    state.userEmail.isNotBlank() -> state.userEmail
    else -> "Signed in user"
}

private fun formatMinutesAsHours(minutes: Int): String =
    if (minutes % 60 == 0) "${minutes / 60} hours/day" else "${minutes} min/day"

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme {
        SettingsContent(
            state = SettingsUiState(isLoading = false, userEmail = "learner@example.com", userName = "Student"),
            appVersion = "1.0",
            onManageProfile = {},
            onReminderToggle = {},
            onReminderTimeClick = {},
            onDailyGoalClick = {},
            onFocusDefaultsClick = {},
            onSignOutClick = {}
        )
    }
}
