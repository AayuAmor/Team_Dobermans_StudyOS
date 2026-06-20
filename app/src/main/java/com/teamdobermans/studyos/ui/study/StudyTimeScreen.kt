package com.teamdobermans.studyos.ui.study

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.StudySession
import com.teamdobermans.studyos.model.Subject
import com.teamdobermans.studyos.viewModel.StudyTimeViewModel

private val BrandPurple   = Color(0xFF7C5CBF)
private val Background    = Color(0xFF1C1C1E)
private val Surface       = Color(0xFF2C2C2E)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun StudyTimeScreen(vm: StudyTimeViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    if (state.showAddSubjectDialog) {
        AddSubjectDialog(
            value = state.newSubjectName,
            onValueChange = vm::onNewSubjectNameChange,
            onConfirm = vm::addSubject,
            onDismiss = { vm.showAddSubjectDialog(false) }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            Text(
                "Study Time Tracking",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            TimerCard(
                elapsedMillis = state.elapsedMillis,
                isRunning = state.isRunning,
                selectedSubject = state.selectedSubject,
                onStart = vm::startTimer,
                onStop = vm::stopTimer
            )
        }

        // Always show subjects section
        item {
            SubjectPicker(
                subjects = state.subjects,
                selected = state.selectedSubject,
                isRunning = state.isRunning,
                onSelect = vm::selectSubject,
                onAdd = { vm.showAddSubjectDialog(true) },
                onDelete = vm::deleteSubject
            )
        }

        // Subject totals
        val completedSessions = state.sessions.filter { !it.isActive }
        if (completedSessions.isNotEmpty()) {
            item {
                Text(
                    "Subject totals",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
            items(state.subjects) { subject ->
                val total = completedSessions
                    .filter { it.subjectId == subject.id }
                    .sumOf { it.durationMillis }
                if (total > 0) SubjectTotalRow(subject, total)
            }
        }

        // Recent sessions
        val recent = completedSessions
            .sortedByDescending { it.startTimeMillis }
            .take(10)
        if (recent.isNotEmpty()) {
            item {
                Text(
                    "Recent sessions",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
            items(recent) { session ->
                SessionRow(session = session, onDelete = { vm.deleteSession(session.id) })
            }
        }
    }
}

@Composable
private fun TimerCard(
    elapsedMillis: Long,
    isRunning: Boolean,
    selectedSubject: Subject?,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val scale by animateFloatAsState(if (isRunning) 0.95f else 1f, tween(150), label = "")
    val ringColor by animateColorAsState(
        if (isRunning) BrandPurple else Surface, tween(400), label = ""
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .border(3.dp, ringColor, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        elapsedMillis.toTimerString(),
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedSubject != null) {
                        Text(selectedSubject.name, color = BrandPurple, fontSize = 12.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) Color(0xFFE07B54) else BrandPurple)
                    .clickable(enabled = selectedSubject != null || isRunning) {
                        if (isRunning) onStop() else onStart()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Stop" else "Start",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            if (!isRunning && selectedSubject == null) {
                Text(
                    "Select a subject below to start",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SubjectPicker(
    subjects: List<Subject>,
    selected: Subject?,
    isRunning: Boolean,
    onSelect: (Subject) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                "Subjects",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = onAdd,
                enabled = !isRunning,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, "Add subject", tint = BrandPurple)
            }
        }

        // Show all subjects as chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(subjects) { subject ->
                val isSelected = selected?.id == subject.id
                val bg by animateColorAsState(
                    if (isSelected) BrandPurple else Surface, label = ""
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else Color(0xFF3A3A3C),
                            RoundedCornerShape(50)
                        )
                        .clickable(enabled = !isRunning) { onSelect(subject) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        subject.name,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (!isRunning) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete ${subject.name}",
                            tint = if (isSelected) Color.White.copy(.7f)
                            else TextSecondary.copy(.4f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onDelete(subject.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectTotalRow(subject: Subject, totalMillis: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(BrandPurple))
        Spacer(Modifier.width(12.dp))
        Text(
            subject.name,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            totalMillis.toTimerString(),
            color = BrandPurple,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SessionRow(session: StudySession, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(session.subjectName, color = TextPrimary, fontSize = 14.sp,
                fontWeight = FontWeight.Medium)
            Text(session.startTimeMillis.toDateString(), color = TextSecondary, fontSize = 12.sp)
        }
        Text(session.durationMillis.toTimerString(), color = TextSecondary, fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Default.Delete, "Delete session",
            tint = TextSecondary.copy(.5f),
            modifier = Modifier.size(18.dp).clickable { onDelete() }
        )
    }
}

@Composable
private fun AddSubjectDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text("New subject", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("e.g. Chemistry", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = Color(0xFF3A3A3C),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BrandPurple
                ),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = value.isNotBlank()) {
                Text("Add", color = BrandPurple, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun Long.toTimerString(): String {
    val s = this / 1_000
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, sec)
    else "%02d:%02d".format(m, sec)
}

private fun Long.toDateString(): String =
    java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(this))