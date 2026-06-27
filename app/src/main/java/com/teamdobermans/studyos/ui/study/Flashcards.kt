package com.teamdobermans.studyos.ui.study

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.ui.components.StudyOSOutlinedButton
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.components.StudyOSSecondaryButton
import com.teamdobermans.studyos.ui.components.StudyOSTextButton
import com.teamdobermans.studyos.ui.theme.PriorityHigh
import com.teamdobermans.studyos.ui.theme.PriorityHighBg
import com.teamdobermans.studyos.ui.theme.PriorityLow
import com.teamdobermans.studyos.ui.theme.PriorityLowBg
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyCardBg
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextHint
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.FlashcardCountOption
import com.teamdobermans.studyos.viewModel.FlashcardDifficultyOption
import com.teamdobermans.studyos.viewModel.FlashcardNoteGroup
import com.teamdobermans.studyos.viewModel.FlashcardQuestionStyle
import com.teamdobermans.studyos.viewModel.FlashcardScreenMode
import com.teamdobermans.studyos.viewModel.FlashcardStats
import com.teamdobermans.studyos.viewModel.FlashcardTab
import com.teamdobermans.studyos.viewModel.FlashcardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Flashcards : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                FlashcardsScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun FlashcardsScreen(
    onBack: () -> Unit = {},
    onCreateNote: () -> Unit = {},
    viewModel: FlashcardViewModel = viewModel()
) {
    val mode by viewModel.mode.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val dueCards by viewModel.dueCards.collectAsState()
    val currentCard by viewModel.currentCard.collectAsState()
    val notes by viewModel.filteredNotes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val notesLoading by viewModel.notesLoading.collectAsState()
    val cardsLoading by viewModel.cardsLoading.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val noteSearchQuery by viewModel.noteSearchQuery.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val countOption by viewModel.countOption.collectAsState()
    val difficulty by viewModel.difficultyOption.collectAsState()
    val questionStyle by viewModel.questionStyle.collectAsState()
    val generatedCards by viewModel.generatedCards.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val noteGroups by viewModel.noteGroups.collectAsState()

    FlashcardsContent(
        mode = mode,
        selectedTab = selectedTab,
        stats = stats,
        allCards = allCards,
        dueCards = dueCards,
        currentCard = currentCard,
        notes = notes,
        folders = folders,
        notesLoading = notesLoading,
        cardsLoading = cardsLoading,
        selectedNote = selectedNote,
        noteSearchQuery = noteSearchQuery,
        selectedFolder = selectedFolder,
        countOption = countOption,
        difficulty = difficulty,
        questionStyle = questionStyle,
        generatedCards = generatedCards,
        isGenerating = isGenerating,
        isSaving = isSaving,
        errorMessage = errorMessage,
        noteGroups = noteGroups,
        onBack = onBack,
        onCreateNote = onCreateNote,
        onCreateFromNotes = viewModel::showNotePicker,
        onDashboard = viewModel::showDashboard,
        onReview = viewModel::showReview,
        onSelectTab = viewModel::selectTab,
        onSearch = viewModel::setNoteSearchQuery,
        onFolder = viewModel::selectFolder,
        onSelectNote = viewModel::selectNote,
        onCountOption = viewModel::setCountOption,
        onDifficulty = viewModel::setDifficultyOption,
        onQuestionStyle = viewModel::setQuestionStyle,
        onGenerate = viewModel::generatePreview,
        onRegenerate = viewModel::regeneratePreview,
        onSaveGenerated = { viewModel.saveGeneratedCards() },
        onUpdateGenerated = viewModel::updateGeneratedCard,
        onRemoveGenerated = viewModel::removeGeneratedCard,
        onSubmitReview = viewModel::submitReview,
        onRetry = viewModel::refreshCards,
        onClearError = viewModel::clearError
    )
}

@Composable
private fun FlashcardsContent(
    mode: FlashcardScreenMode,
    selectedTab: FlashcardTab,
    stats: FlashcardStats,
    allCards: List<FlashcardModel>,
    dueCards: List<FlashcardModel>,
    currentCard: FlashcardModel?,
    notes: List<NoteModel>,
    folders: List<String>,
    notesLoading: Boolean,
    cardsLoading: Boolean,
    selectedNote: NoteModel?,
    noteSearchQuery: String,
    selectedFolder: String?,
    countOption: FlashcardCountOption,
    difficulty: FlashcardDifficultyOption,
    questionStyle: FlashcardQuestionStyle,
    generatedCards: List<FlashcardModel>,
    isGenerating: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    noteGroups: List<FlashcardNoteGroup>,
    onBack: () -> Unit,
    onCreateNote: () -> Unit,
    onCreateFromNotes: () -> Unit,
    onDashboard: () -> Unit,
    onReview: () -> Unit,
    onSelectTab: (FlashcardTab) -> Unit,
    onSearch: (String) -> Unit,
    onFolder: (String?) -> Unit,
    onSelectNote: (NoteModel) -> Unit,
    onCountOption: (FlashcardCountOption) -> Unit,
    onDifficulty: (FlashcardDifficultyOption) -> Unit,
    onQuestionStyle: (FlashcardQuestionStyle) -> Unit,
    onGenerate: () -> Unit,
    onRegenerate: () -> Unit,
    onSaveGenerated: () -> Unit,
    onUpdateGenerated: (Int, String, String) -> Unit,
    onRemoveGenerated: (Int) -> Unit,
    onSubmitReview: (FlashcardModel, Int) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        FlashcardsHeader(onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StatsRow(stats = stats) }

            if (errorMessage != null) {
                item { ErrorCard(message = errorMessage, onRetry = onRetry, onDismiss = onClearError) }
            }

            when (mode) {
                FlashcardScreenMode.Dashboard -> {
                    item { CreateFromNotesCard(onClick = onCreateFromNotes) }
                    item { FlashcardTabs(selectedTab = selectedTab, onSelect = onSelectTab) }
                    when (selectedTab) {
                        FlashcardTab.DueToday -> item {
                            DueTodaySection(
                                currentCard = currentCard,
                                dueCount = dueCards.size,
                                cardsLoading = cardsLoading,
                                onReview = onReview,
                                onCreateMore = onCreateFromNotes,
                                onSubmitReview = onSubmitReview
                            )
                        }

                        FlashcardTab.AllCards -> {
                            if (allCards.isEmpty()) item {
                                EmptyStateCard(
                                    title = "No flashcards yet",
                                    description = "Select a note and generate your first revision cards.",
                                    buttonText = "Create from Notes",
                                    onClick = onCreateFromNotes
                                )
                            } else items(allCards, key = { it.id.ifBlank { it.question + it.createdAt } }) { card ->
                                SavedFlashcardCard(card = card)
                            }
                        }

                        FlashcardTab.ByNote -> {
                            if (noteGroups.isEmpty()) item {
                                EmptyStateCard(
                                    title = "No flashcards yet",
                                    description = "Saved cards will be grouped by their source note here.",
                                    buttonText = "Create from Notes",
                                    onClick = onCreateFromNotes
                                )
                            } else items(noteGroups, key = { it.noteId }) { group ->
                                NoteGroupCard(group = group, onReview = { onSelectTab(FlashcardTab.DueToday) })
                            }
                        }
                    }
                }

                FlashcardScreenMode.SelectNote -> {
                    item {
                        NotePickerSection(
                            notes = notes,
                            folders = folders,
                            notesLoading = notesLoading,
                            query = noteSearchQuery,
                            selectedFolder = selectedFolder,
                            cardCounts = allCards.groupingBy { it.noteId }.eachCount(),
                            onQuery = onSearch,
                            onFolder = onFolder,
                            onSelectNote = onSelectNote,
                            onCreateNote = onCreateNote,
                            onBack = onDashboard
                        )
                    }
                }

                FlashcardScreenMode.GenerateOptions -> item {
                    GenerateOptionsSection(
                        note = selectedNote,
                        countOption = countOption,
                        difficulty = difficulty,
                        questionStyle = questionStyle,
                        isGenerating = isGenerating,
                        onCountOption = onCountOption,
                        onDifficulty = onDifficulty,
                        onQuestionStyle = onQuestionStyle,
                        onGenerate = {
                            when {
                                isGenerating -> Toast.makeText(
                                    context,
                                    "Please wait, flashcards are being generated",
                                    Toast.LENGTH_SHORT
                                ).show()

                                selectedNote == null -> Toast.makeText(
                                    context,
                                    "Please select a note first",
                                    Toast.LENGTH_SHORT
                                ).show()

                                selectedNote.body.split(Regex("\\s+")).count { it.isNotBlank() } < 12 -> Toast.makeText(
                                    context,
                                    "This note is too short to generate flashcards",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> onGenerate()
                            }
                        },
                        onChangeNote = onCreateFromNotes
                    )
                }

                FlashcardScreenMode.PreviewGenerated -> item {
                    PreviewGeneratedSection(
                        cards = generatedCards,
                        isSaving = isSaving,
                        onUpdate = onUpdateGenerated,
                        onRemove = onRemoveGenerated,
                        onRegenerate = {
                            if (isGenerating) {
                                Toast.makeText(
                                    context,
                                    "Please wait, flashcards are being generated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onRegenerate()
                            }
                        },
                        onSave = {
                            when {
                                isSaving -> Toast.makeText(
                                    context,
                                    "Please wait, this is already processing",
                                    Toast.LENGTH_SHORT
                                ).show()

                                generatedCards.isEmpty() -> Toast.makeText(
                                    context,
                                    "No flashcards available to save",
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> onSaveGenerated()
                            }
                        }
                    )
                }

                FlashcardScreenMode.Review -> item {
                    ReviewSection(
                        currentCard = currentCard,
                        dueCount = dueCards.size,
                        onSubmitReview = onSubmitReview,
                        onCreateMore = onCreateFromNotes,
                        onBack = onDashboard
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp)
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.16f),
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Flashcards", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Turn your notes into quick revision cards.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StatsRow(stats: FlashcardStats) {
    StudyCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Total Cards", stats.totalCards.toString())
            StatDivider()
            StatItem("Due Today", stats.dueToday.toString())
            StatDivider()
            StatItem("Mastered", stats.mastered.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = StudyPurple, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(38.dp).background(StudyPurpleLight))
}

@Composable
private fun CreateFromNotesCard(onClick: () -> Unit) {
    StudyCard {
        Text("Create from Notes", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Select a note and generate flashcards for revision.", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(14.dp))
        StudyOSPrimaryButton(text = "Choose Note", onClick = onClick, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun FlashcardTabs(selectedTab: FlashcardTab, onSelect: (FlashcardTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TabChip(
            "Due Today",
            selectedTab == FlashcardTab.DueToday,
            Modifier.weight(1f)
        ) { onSelect(FlashcardTab.DueToday) }
        TabChip(
            "All Cards",
            selectedTab == FlashcardTab.AllCards,
            Modifier.weight(1f)
        ) { onSelect(FlashcardTab.AllCards) }
        TabChip("By Note", selectedTab == FlashcardTab.ByNote, Modifier.weight(1f)) { onSelect(FlashcardTab.ByNote) }
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = if (selected) StudyPurple else StudyCardBg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) Color.White else StudyPurple,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DueTodaySection(
    currentCard: FlashcardModel?,
    dueCount: Int,
    cardsLoading: Boolean,
    onReview: () -> Unit,
    onCreateMore: () -> Unit,
    onSubmitReview: (FlashcardModel, Int) -> Unit
) {
    if (cardsLoading) {
        LoadingCard("Loading flashcards…")
    } else if (currentCard == null) {
        EmptyStateCard(
            title = "All caught up",
            description = "You have no flashcards due for review today.",
            buttonText = "Create More Flashcards",
            onClick = onCreateMore
        )
    } else {
        StudyCard {
            Text("Due Today", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "$dueCount card${if (dueCount == 1) "" else "s"} ready for review",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReviewCard(card = currentCard, onSubmitReview = onSubmitReview)
            Spacer(modifier = Modifier.height(12.dp))
            StudyOSPrimaryButton(text = "Review Due Cards", onClick = onReview, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReviewSection(
    currentCard: FlashcardModel?,
    dueCount: Int,
    onSubmitReview: (FlashcardModel, Int) -> Unit,
    onCreateMore: () -> Unit,
    onBack: () -> Unit
) {
    if (currentCard == null) {
        EmptyStateCard(
            title = "All caught up",
            description = "You have no flashcards due for review today.",
            buttonText = "Create More Flashcards",
            onClick = onCreateMore
        )
        Spacer(modifier = Modifier.height(8.dp))
        StudyOSOutlinedButton(text = "Back to Flashcards", onClick = onBack, modifier = Modifier.fillMaxWidth())
    } else {
        StudyCard {
            Text("Due Today", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("$dueCount card${if (dueCount == 1) "" else "s"} remaining", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(14.dp))
            ReviewCard(card = currentCard, onSubmitReview = onSubmitReview)
            Spacer(modifier = Modifier.height(12.dp))
            StudyOSOutlinedButton(text = "Back to Flashcards", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReviewCard(card: FlashcardModel, onSubmitReview: (FlashcardModel, Int) -> Unit) {
    var showAnswer by remember(card.id, card.question) { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(20.dp), color = StudyPurpleFaint, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (showAnswer) "Answer" else "Question",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (showAnswer) card.answer else card.question,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!showAnswer) {
                StudyOSSecondaryButton(
                    text = "Reveal Answer",
                    onClick = { showAnswer = true },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReviewActionButton(
                        "Again",
                        PriorityHighBg,
                        PriorityHigh,
                        Modifier.weight(1f)
                    ) { onSubmitReview(card, 1) }
                    ReviewActionButton("Got It", PriorityLowBg, PriorityLow, Modifier.weight(1f)) {
                        onSubmitReview(
                            card,
                            5
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewActionButton(
    text: String,
    background: Color,
    content: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        color = background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = content, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NotePickerSection(
    notes: List<NoteModel>,
    folders: List<String>,
    notesLoading: Boolean,
    query: String,
    selectedFolder: String?,
    cardCounts: Map<String, Int>,
    onQuery: (String) -> Unit,
    onFolder: (String?) -> Unit,
    onSelectNote: (NoteModel) -> Unit,
    onCreateNote: () -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("Select a Note", "Choose a saved note to create flashcards from.")
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = StudyPurple) },
            trailingIcon = {
                if (query.isNotBlank()) Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = TextHint,
                    modifier = Modifier.clickable { onQuery("") })
            },
            placeholder = { Text("Search notes", color = TextHint) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudyPurple,
                unfocusedBorderColor = StudyPurpleLight,
                focusedContainerColor = StudyCardBg,
                unfocusedContainerColor = StudyCardBg,
                cursorColor = StudyPurple
            )
        )
        if (folders.isNotEmpty()) {
            LazyColumn(modifier = Modifier.heightIn(max = 104.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip("All folders", selectedFolder == null) { onFolder(null) } }
                items(folders) { folder -> FilterChip(folder, selectedFolder == folder) { onFolder(folder) } }
            }
        }
        when {
            notesLoading -> LoadingCard("Loading notes…")
            notes.isEmpty() -> EmptyStateCard(
                title = "No notes found",
                description = "Create notes first to generate flashcards.",
                buttonText = "Create Note",
                onClick = onCreateNote
            )

            else -> notes.forEach { note ->
                NotePickerCard(
                    note = note,
                    flashcardCount = cardCounts[note.id] ?: 0,
                    onSelect = { onSelectNote(note) })
            }
        }
        StudyOSOutlinedButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        color = if (selected) StudyPurple else StudyCardBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.White else StudyPurple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun NotePickerCard(note: NoteModel, flashcardCount: Int, onSelect: () -> Unit) {
    val date = note.lastActivityTime().takeIf { it > 0L }
        ?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "No date"
    StudyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.title.ifBlank { "Untitled Note" },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.folder.isNotBlank()) Text(
                    note.folder,
                    color = StudyPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(date, color = TextHint, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            note.body.replace('\n', ' ').ifBlank { "No preview available" },
            color = TextSecondary,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "$flashcardCount existing flashcard${if (flashcardCount == 1) "" else "s"}",
            color = TextHint,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        StudyOSPrimaryButton(text = "Use this Note", onClick = onSelect, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun GenerateOptionsSection(
    note: NoteModel?,
    countOption: FlashcardCountOption,
    difficulty: FlashcardDifficultyOption,
    questionStyle: FlashcardQuestionStyle,
    isGenerating: Boolean,
    onCountOption: (FlashcardCountOption) -> Unit,
    onDifficulty: (FlashcardDifficultyOption) -> Unit,
    onQuestionStyle: (FlashcardQuestionStyle) -> Unit,
    onGenerate: () -> Unit,
    onChangeNote: () -> Unit
) {
    if (note == null) {
        EmptyStateCard("No note selected", "Choose a note before generating flashcards.", "Choose Note", onChangeNote)
        return
    }
    StudyCard {
        SectionHeader(
            "Generate Flashcards",
            "Create revision cards from: \"${note.title.ifBlank { "Untitled Note" }}\""
        )
        OptionGroup("Number of cards") {
            OptionChip("5", countOption == FlashcardCountOption.Five) { onCountOption(FlashcardCountOption.Five) }
            OptionChip("10", countOption == FlashcardCountOption.Ten) { onCountOption(FlashcardCountOption.Ten) }
            OptionChip(
                "15",
                countOption == FlashcardCountOption.Fifteen
            ) { onCountOption(FlashcardCountOption.Fifteen) }
            OptionChip("Auto", countOption == FlashcardCountOption.Auto) { onCountOption(FlashcardCountOption.Auto) }
        }
        OptionGroup("Difficulty") {
            OptionChip(
                "Basic",
                difficulty == FlashcardDifficultyOption.Basic
            ) { onDifficulty(FlashcardDifficultyOption.Basic) }
            OptionChip("Medium", difficulty == FlashcardDifficultyOption.Medium) {
                onDifficulty(
                    FlashcardDifficultyOption.Medium
                )
            }
            OptionChip("Exam Style", difficulty == FlashcardDifficultyOption.ExamStyle) {
                onDifficulty(
                    FlashcardDifficultyOption.ExamStyle
                )
            }
        }
        OptionGroup("Question style") {
            OptionChip("Definition", questionStyle == FlashcardQuestionStyle.Definition) {
                onQuestionStyle(
                    FlashcardQuestionStyle.Definition
                )
            }
            OptionChip("Short Answer", questionStyle == FlashcardQuestionStyle.ShortAnswer) {
                onQuestionStyle(
                    FlashcardQuestionStyle.ShortAnswer
                )
            }
            OptionChip("Concept Check", questionStyle == FlashcardQuestionStyle.ConceptCheck) {
                onQuestionStyle(
                    FlashcardQuestionStyle.ConceptCheck
                )
            }
            OptionChip(
                "Mixed",
                questionStyle == FlashcardQuestionStyle.Mixed
            ) { onQuestionStyle(FlashcardQuestionStyle.Mixed) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        StudyOSPrimaryButton(
            text = "Generate Flashcards",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            isLoading = isGenerating
        )
        Spacer(modifier = Modifier.height(8.dp))
        StudyOSOutlinedButton(text = "Change Note", onClick = onChangeNote, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OptionGroup(title: String, content: @Composable RowScope.() -> Unit) {
    Spacer(modifier = Modifier.height(14.dp))
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun OptionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        color = if (selected) StudyPurple else StudyPurpleLight,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.White else StudyPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun PreviewGeneratedSection(
    cards: List<FlashcardModel>,
    isSaving: Boolean,
    onUpdate: (Int, String, String) -> Unit,
    onRemove: (Int) -> Unit,
    onRegenerate: () -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Generated Flashcards", "Review before saving.")
        if (cards.isEmpty()) {
            EmptyStateCard(
                "No generated cards",
                "Regenerate or choose a note with more content.",
                "Regenerate",
                onRegenerate
            )
        } else {
            cards.forEachIndexed { index, card -> GeneratedFlashcardCard(index, card, onUpdate, onRemove) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StudyOSOutlinedButton(text = "Regenerate", onClick = onRegenerate, modifier = Modifier.weight(1f))
                StudyOSPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Flashcards",
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    isLoading = isSaving
                )
            }
        }
    }
}

@Composable
private fun GeneratedFlashcardCard(
    index: Int,
    card: FlashcardModel,
    onUpdate: (Int, String, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    var editing by remember(index) { mutableStateOf(false) }
    var question by remember(card.question) { mutableStateOf(card.question) }
    var answer by remember(card.answer) { mutableStateOf(card.answer) }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit flashcard", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                StudyOSTextButton(
                    "Save",
                    onClick = { onUpdate(index, question, answer); editing = false })
            },
            dismissButton = { StudyOSTextButton("Cancel", onClick = { editing = false }) }
        )
    }

    StudyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text("Card ${index + 1}", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    tint = StudyPurple,
                    modifier = Modifier.size(20.dp).clickable { editing = true })
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Remove",
                    tint = PriorityHigh,
                    modifier = Modifier.size(20.dp).clickable { onRemove(index) })
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Question", color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(card.question, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Answer", color = TextHint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(card.answer, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SavedFlashcardCard(card: FlashcardModel) {
    StudyCard {
        Text(card.question, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(card.answer, color = TextSecondary, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (card.noteTitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("From: ${card.noteTitle}", color = TextHint, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NoteGroupCard(group: FlashcardNoteGroup, onReview: () -> Unit) {
    val context = LocalContext.current

    StudyCard {
        Text(group.noteTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${group.cards.size} cards · ${group.dueCount} due", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StudyOSPrimaryButton(
                text = "Review",
                onClick = {
                    if (group.dueCount <= 0) {
                        Toast.makeText(context, "You have no flashcards due for review today", Toast.LENGTH_SHORT)
                            .show()
                        return@StudyOSPrimaryButton
                    }
                    onReview()
                },
                modifier = Modifier.weight(1f)
            )
            StudyOSSecondaryButton(text = "View Cards", onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF0F0), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Something went wrong", color = PriorityHigh, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudyOSOutlinedButton("Retry", onClick = onRetry, modifier = Modifier.weight(1f))
                StudyOSSecondaryButton("Dismiss", onClick = onDismiss, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    StudyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = StudyPurple, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    StudyCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(14.dp))
            StudyOSPrimaryButton(text = buttonText, onClick = onClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun StudyCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = StudyCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

private fun NoteModel.lastActivityTime(): Long = when {
    updatedAt > 0L -> updatedAt
    timestamp > 0L -> timestamp
    createdAt > 0L -> createdAt
    else -> 0L
}

@Preview(showBackground = true)
@Composable
fun FlashcardsScreenPreview() {
    StudyOSTheme {
        FlashcardsContent(
            mode = FlashcardScreenMode.Dashboard,
            selectedTab = FlashcardTab.DueToday,
            stats = FlashcardStats(totalCards = 2, dueToday = 1, mastered = 0),
            allCards = listOf(FlashcardModel(question = "What is a queue?", answer = "A FIFO data structure.")),
            dueCards = listOf(FlashcardModel(question = "What is a queue?", answer = "A FIFO data structure.")),
            currentCard = FlashcardModel(question = "What is a queue?", answer = "A FIFO data structure."),
            notes = emptyList(),
            folders = emptyList(),
            notesLoading = false,
            cardsLoading = false,
            selectedNote = null,
            noteSearchQuery = "",
            selectedFolder = null,
            countOption = FlashcardCountOption.Auto,
            difficulty = FlashcardDifficultyOption.Basic,
            questionStyle = FlashcardQuestionStyle.Mixed,
            generatedCards = emptyList(),
            isGenerating = false,
            isSaving = false,
            errorMessage = null,
            noteGroups = emptyList(),
            onBack = {},
            onCreateNote = {},
            onCreateFromNotes = {},
            onDashboard = {},
            onReview = {},
            onSelectTab = {},
            onSearch = {},
            onFolder = {},
            onSelectNote = {},
            onCountOption = {},
            onDifficulty = {},
            onQuestionStyle = {},
            onGenerate = {},
            onRegenerate = {},
            onSaveGenerated = {},
            onUpdateGenerated = { _, _, _ -> },
            onRemoveGenerated = {},
            onSubmitReview = { _, _ -> },
            onRetry = {},
            onClearError = {}
        )
    }
}
