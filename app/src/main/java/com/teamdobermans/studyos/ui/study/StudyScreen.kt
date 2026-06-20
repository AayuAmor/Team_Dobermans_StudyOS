package com.teamdobermans.studyos.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.StudyUiState
import com.teamdobermans.studyos.viewModel.StudyViewModel
import com.teamdobermans.studyos.viewModel.SubjectUiModel

@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    onNavigateNotes: () -> Unit = {},
    onNavigateFlashcards: () -> Unit = {},
    onNavigateMockTest: () -> Unit = {},
    onNavigateVideoNotes: () -> Unit = {},
    onNavigateStudyTime: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val subjects = viewModel.filteredSubjects

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { StudyHeader(query = state.searchQuery, onQueryChange = viewModel::updateSearch) }

        if (state.searchQuery.isBlank()) {
            item {
                SubjectsSection(subjects = subjects, onNotesTap = onNavigateNotes)
            }
            item {
                RecentNotesSection(
                    noteTitle  = state.recentNoteTitle,
                    noteFolder = state.recentNoteFolder,
                    onTap      = onNavigateNotes
                )
            }
            item {
                AiToolsSection(
                    onVideoNotes  = onNavigateVideoNotes,
                    onFlashcards  = onNavigateFlashcards,
                    onQuiz        = onNavigateMockTest,
                    onNotes       = onNavigateNotes,
                    onStudyTime   = onNavigateStudyTime
                )
            }
        } else {
            item {
                SearchResultsHeader(query = state.searchQuery, subjects = subjects)
            }
            items(subjects) { subject ->
                SubjectSearchRow(subject = subject, onClick = onNavigateNotes)
            }
        }
    }
}

@Composable
private fun StudyHeader(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(colors = listOf(StudyPurpleDeep, StudyPurple))
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
    ) {
        Column {
            Text(
                text  = "Study",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "Your knowledge hub",
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                placeholder = {
                    Text(
                        "Search notes, subjects, flashcards…",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.60f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.60f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.White.copy(alpha = 0.18f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.18f),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    cursorColor             = Color.White
                )
            )
        }
    }
}

@Composable
private fun SubjectsSection(subjects: List<SubjectUiModel>, onNotesTap: () -> Unit) {
    Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Subjects", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                text  = "See all",
                color = StudyPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNotesTap() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(subjects) { subject ->
                SubjectCard(subject = subject, onClick = onNotesTap)
            }
        }
    }
}

@Composable
private fun SubjectCard(subject: SubjectUiModel, onClick: () -> Unit) {
    val subjectColor = Color(subject.colorArgb)
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(subjectColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = subjectColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = subject.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountPill(label = "${subject.notesCount} notes", color = subjectColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { subject.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color      = subjectColor,
                trackColor = subjectColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "${(subject.progressPercent * 100).toInt()}% complete",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CountPill(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.10f)) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun RecentNotesSection(
    noteTitle: String?,
    noteFolder: String?,
    onTap: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Notes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                text  = "All notes",
                color = StudyPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onTap() }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (noteTitle != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(14.dp))
                    .clickable { onTap() },
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudyPurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = StudyPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = noteTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (noteFolder != null) {
                            Text(text = noteFolder, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = TextHint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable { onTap() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No notes yet — tap to create one!", color = TextHint, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AiToolsSection(
    onVideoNotes: () -> Unit,
    onFlashcards: () -> Unit,
    onQuiz: () -> Unit,
    onNotes: () -> Unit,
    onStudyTime: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = StudyPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("AI Tools", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiToolCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Rounded.VideoLibrary,
                iconBg    = Color(0xFFE8F4FF),
                iconTint  = Color(0xFF3A82E0),
                title     = "Video → Notes",
                subtitle  = "YouTube / Upload",
                onClick   = onVideoNotes
            )
            AiToolCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Rounded.Timer,
                iconBg    = Color(0xFFF0E8FF),
                iconTint  = StudyPurple,
                title     = "Study Timer",
                subtitle  = "Track sessions",
                onClick   = onStudyTime
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiToolCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Rounded.Style,
                iconBg    = Color(0xFFFFF0E0),
                iconTint  = Color(0xFFE07B39),
                title     = "Flashcards",
                subtitle  = "From your notes",
                onClick   = onFlashcards
            )
            AiToolCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Rounded.Quiz,
                iconBg    = Color(0xFFE8FFF4),
                iconTint  = Color(0xFF1D9E75),
                title     = "Generate Quiz",
                subtitle  = "Test yourself",
                onClick   = onQuiz
            )
        }
    }
}

@Composable
private fun AiToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(text = title,    fontSize = 12.sp, fontWeight = FontWeight.Bold,    color = TextPrimary)
            Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SearchResultsHeader(query: String, subjects: List<SubjectUiModel>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            text  = "Results for \"$query\"",
            color = StudyPurple,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (subjects.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("No subjects found.", color = TextHint, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SubjectSearchRow(subject: SubjectUiModel, onClick: () -> Unit) {
    val color = Color(subject.colorArgb)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = subject.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = "${subject.notesCount} notes • ${subject.flashcardsCount} cards", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
        }
    }
}

