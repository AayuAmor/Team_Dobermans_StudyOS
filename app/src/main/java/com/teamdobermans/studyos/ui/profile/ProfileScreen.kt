package com.teamdobermans.studyos.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.ui.components.StudyOSDestructiveButton
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.components.StudyOSTextButton
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.ProfileViewModel

@Composable
fun ProfileScreenV2(
    viewModel: ProfileViewModel,
    onNavigateAnalytics: () -> Unit = {},
    onNavigateVisionBoard: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val email by viewModel.email.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveResult) {
        saveResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearResult()
        }
    }

    val displayName = email.substringBefore("@").ifEmpty { "User" }
    val initials = displayName.take(2).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(initials = initials, displayName = displayName, email = email)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            AccountIdentityCard(displayName = displayName, email = email)

            Spacer(modifier = Modifier.height(12.dp))

            QuickLinksCard(
                onAnalytics = onNavigateAnalytics,
                onVisionBoard = onNavigateVisionBoard,
                onSettings = onNavigateSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            AccountSettingsCard(
                displayName = displayName,
                onSaveName = { name -> if (name.isNotBlank()) viewModel.updateDisplayName(name) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SignOutCard(onSignOut = onSignOut)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileHeader(initials: String, displayName: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = email, color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(14.dp))
            StudyBadge(icon = Icons.Rounded.VerifiedUser, label = "StudyOS account")
        }
    }
}

@Composable
private fun StudyBadge(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AccountIdentityCard(displayName: String, email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionTitle(icon = Icons.Rounded.Badge, title = "Student identity")
            Spacer(modifier = Modifier.height(10.dp))
            Text(displayName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(email.ifBlank { "No email available" }, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = StudyPurple)
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StudyPurpleLight))
}

@Composable
private fun QuickLinksCard(
    onAnalytics: () -> Unit,
    onVisionBoard: () -> Unit,
    onSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(icon = Icons.Rounded.Apps, title = "Quick links")
            Spacer(modifier = Modifier.height(8.dp))
            ProfileLinkRow(Icons.Rounded.Analytics, "Analytics", "Track real study performance", onAnalytics)
            HorizontalDivider(color = StudyPurpleFaint)
            ProfileLinkRow(Icons.Rounded.Star, "Vision Board", "Manage goals and motivation", onVisionBoard)
            HorizontalDivider(color = StudyPurpleFaint)
            ProfileLinkRow(Icons.Rounded.Settings, "Settings", "Reminders, account, and sync", onSettings)
        }
    }
}

@Composable
private fun ProfileLinkRow(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(StudyPurpleFaint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(description, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AnalyticsAccessCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPurple)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Analytics", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Track your study performance", color = Color.White.copy(alpha = 0.70f), fontSize = 11.sp)
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun VisionBoardAccessCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudyPurpleFaint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = StudyPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Vision Board", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Manage your goals and motivation", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AccountSettingsCard(displayName: String, onSaveName: (String) -> Unit) {
    var editName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(icon = Icons.Rounded.ManageAccounts, title = "Account")
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("Display name (current: $displayName)", color = TextHint, fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = StudyPurpleFaint,
                    focusedContainerColor = StudyPurpleFaint,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = StudyPurple
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            StudyOSPrimaryButton(
                text = "Save Name",
                onClick = {
                    when {
                        editName.isBlank() -> Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT)
                            .show()

                        editName.trim() == displayName -> Toast.makeText(
                            context,
                            "No changes to save",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> {
                            onSaveName(editName.trim())
                            editName = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun SignOutCard(onSignOut: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Sign Out?") },
            text = { Text("You'll need to sign in again to access your data.") },
            confirmButton = {
                StudyOSTextButton(text = "Sign Out", onClick = { showDialog = false; onSignOut() })
            },
            dismissButton = {
                StudyOSTextButton(text = "Cancel", onClick = { showDialog = false })
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PriorityHighBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = PriorityHigh,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign Out",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PriorityHigh,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = PriorityHigh,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = StudyPurple, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
