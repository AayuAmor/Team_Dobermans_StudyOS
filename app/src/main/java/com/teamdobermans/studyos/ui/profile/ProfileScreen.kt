package com.teamdobermans.studyos.ui.profile

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.components.StudyOSDestructiveButton
import com.teamdobermans.studyos.ui.components.StudyOSOutlinedButton
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.components.StudyOSTextButton
import com.teamdobermans.studyos.ui.theme.PriorityHigh
import com.teamdobermans.studyos.ui.theme.PriorityHighBg
import com.teamdobermans.studyos.ui.theme.StudyCardBg
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextHint
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.ProfileViewModel

@Composable
fun ProfileScreenV2(
    viewModel: ProfileViewModel,
    onSignOut: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val email by viewModel.email.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val context = LocalContext.current
    var editDialog by remember { mutableStateOf(false) }
    var signOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveResult) {
        saveResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearResult()
        }
    }

    val displayName = profile.name.ifBlank { email.substringBefore("@").ifBlank { "Student" } }
    val displayEmail = email.ifBlank { profile.email }.ifBlank { "Signed in user" }

    if (editDialog) {
        EditDisplayNameDialog(
            currentName = displayName,
            onDismiss = { editDialog = false },
            onSave = { name ->
                when {
                    name.isBlank() -> Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                    name.trim() == displayName -> Toast.makeText(context, "No changes to save", Toast.LENGTH_SHORT)
                        .show()

                    else -> {
                        viewModel.updateDisplayName(name.trim())
                        editDialog = false
                    }
                }
            }
        )
    }

    if (signOutDialog) {
        AlertDialog(
            onDismissRequest = { signOutDialog = false },
            title = { Text("Sign out?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("You will need to sign in again to sync your StudyOS data.", color = TextSecondary) },
            confirmButton = {
                StudyOSDestructiveButton(text = "Sign Out", onClick = { signOutDialog = false; onSignOut() })
            },
            dismissButton = { StudyOSTextButton(text = "Cancel", onClick = { signOutDialog = false }) },
            containerColor = StudyCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(displayName = displayName, email = displayEmail)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileDetailsCard(
                displayName = displayName,
                email = displayEmail,
                onEditName = { editDialog = true }
            )
            AccountStatusCard()
            SignOutCard(onClick = { signOutDialog = true })
        }
    }
}

@Composable
private fun ProfileHeader(displayName: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            InitialsAvatar(displayName, email, size = 82)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                displayName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(email, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProfileDetailsCard(displayName: String, email: String, onEditName: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("User Profile", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            ProfileInfoRow(label = "Display name", value = displayName)
            ProfileInfoRow(label = "Email", value = email)
            ProfileInfoRow(label = "Provider", value = "Firebase Auth")
            StudyOSPrimaryButton(
                text = "Edit display name",
                onClick = onEditName,
                leadingIcon = Icons.Rounded.Edit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AccountStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyCardBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(StudyPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = StudyPurple)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("StudyOS account", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Signed in and syncing with Firebase.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SignOutCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(22.dp)).clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyCardBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(PriorityHighBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = PriorityHigh)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Sign out", color = PriorityHigh, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Sign out from this device.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EditDisplayNameDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit display name", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudyPurple,
                    unfocusedBorderColor = StudyPurpleLight,
                    cursorColor = StudyPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { StudyOSPrimaryButton(text = "Save", onClick = { onSave(name) }) },
        dismissButton = { StudyOSOutlinedButton(text = "Cancel", onClick = onDismiss) },
        containerColor = StudyCardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(StudyPurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Badge, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InitialsAvatar(name: String, email: String, size: Int) {
    val initials = remember(name, email) {
        val source = name.ifBlank { email.substringBefore("@").ifBlank { "Student" } }
        source.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "ST" }
    }
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = (size / 3).sp)
    }
}
