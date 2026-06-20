package com.teamdobermans.studyos.ui.study

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AutoSaveStatus
import com.teamdobermans.studyos.viewModel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Source filter ────────────────────────────────────────────────────────────

private enum class SourceFilter(val label: String, val key: String?) {
    ALL("All", null),
    MANUAL("Manual", "MANUAL"),
    VIDEO("Video Notes", "VIDEO"),
    IMPORTED("Imported", "IMPORTED")
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun wordCount(body: String): Int =
    body.split(Regex("\\s+")).count { it.isNotBlank() }

private fun isQuizReady(body: String): Boolean = wordCount(body) >= 50

// ─── Activity shell ───────────────────────────────────────────────────────────

class NotesPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyOSTheme {
                NotesScreen(onBackClick = { finish() })
            }
        }
    }
}

// ─── Public entry point (used by NavGraph + NotesPage) ────────────────────────

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel? = null,
    onBackClick: () -> Unit = {},
    onNavigateVideoNotes: () -> Unit = {}
) {
    val resolvedVm: NoteViewModel =
        viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()

    val allNotes       by resolvedVm.notes.collectAsState()
    val autoSaveStatus by resolvedVm.autoSaveStatus.collectAsState()

    var searchQuery  by rememberSaveable { mutableStateOf("") }
    var sourceFilter by rememberSaveable { mutableStateOf(SourceFilter.ALL) }
    var selectedNote by remember { mutableStateOf<NoteModel?>(null) }
    var showEditor   by rememberSaveable { mutableStateOf(false) }

    if (showEditor) {
        NoteEditorScreen(
            existingNote   = selectedNote,
            autoSaveStatus = autoSaveStatus,
            onEditorChanged = { noteId, title, body ->
                resolvedVm.onEditorChanged(noteId, title, body, "")
            },
            onSave = { title, body ->
                if (selectedNote != null) {
                    resolvedVm.updateNote(
                        selectedNote!!.copy(
                            title     = title,
                            body      = body,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    resolvedVm.createNote(title, body, "")
                }
                resolvedVm.clearEditingNote()
                showEditor   = false
                selectedNote = null
            },
            onDelete = { noteId ->
                resolvedVm.deleteNote(noteId)
                resolvedVm.clearEditingNote()
                showEditor   = false
                selectedNote = null
            },
            onBack = {
                resolvedVm.clearEditingNote()
                showEditor   = false
                selectedNote = null
            }
        )
        return
    }

    val visibleNotes = remember(allNotes, searchQuery, sourceFilter) {
        var list = allNotes
        if (searchQuery.isNotBlank()) {
            list = list.filter { n ->
                n.title.contains(searchQuery, ignoreCase = true) ||
                n.body.contains(searchQuery, ignoreCase = true)
            }
        }
        if (sourceFilter != SourceFilter.ALL) {
            list = list.filter { n ->
                n.sourceType.equals(sourceFilter.key, ignoreCase = true)
            }
        }
        list
    }

    NotesWorkspaceScreen(
        modifier             = modifier,
        notes                = visibleNotes,
        allNotes             = allNotes,
        searchQuery          = searchQuery,
        sourceFilter         = sourceFilter,
        onSearchChange       = { searchQuery = it },
        onFilterChange       = { sourceFilter = it },
        onBackClick          = onBackClick,
        onNoteClick          = { note -> selectedNote = note; showEditor = true },
        onCreateNote         = { selectedNote = null; showEditor = true },
        onDeleteNote         = { id -> resolvedVm.deleteNote(id) },
        onNavigateVideoNotes = onNavigateVideoNotes
    )
}

// ─── Workspace screen ─────────────────────────────────────────────────────────

@Composable
private fun NotesWorkspaceScreen(
    modifier: Modifier,
    notes: List<NoteModel>,
    allNotes: List<NoteModel>,
    searchQuery: String,
    sourceFilter: SourceFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (SourceFilter) -> Unit,
    onBackClick: () -> Unit,
    onNoteClick: (NoteModel) -> Unit,
    onCreateNote: () -> Unit,
    onDeleteNote: (String) -> Unit,
    onNavigateVideoNotes: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier       = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8F7FC),
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onCreateNote,
                containerColor = BrandPurple,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note", tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Top bar
            item {
                NotesTopBar(onBackClick = onBackClick)
            }

            // Title + subtitle + search
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Text(
                        text       = "Notes",
                        color      = TextPrimary,
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = "Capture, organize, and turn notes into quizzes",
                        color    = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NotesSearchBar(query = searchQuery, onQueryChange = onSearchChange)
                }
            }

            // Source filter chips
            item {
                NoteSourceFilterChips(
                    selected   = sourceFilter,
                    onSelect   = onFilterChange,
                    modifier   = Modifier.background(Color.White)
                )
                HorizontalDivider(color = Color(0xFFEEEAFF))
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Empty state — no notes at all
            if (allNotes.isEmpty() && searchQuery.isBlank()) {
                item {
                    NotesEmptyState(
                        onCreateNote         = onCreateNote,
                        onNavigateVideoNotes = onNavigateVideoNotes,
                        modifier             = Modifier.padding(horizontal = 24.dp)
                    )
                }
                return@LazyColumn
            }

            // Filtered empty state
            if (notes.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp, horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text       = if (searchQuery.isNotBlank())
                                                 "No notes match \"$searchQuery\""
                                             else
                                                 "No ${sourceFilter.label.lowercase()} notes yet",
                                color      = TextSecondary,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign  = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (searchQuery.isBlank()) {
                                Text(
                                    text      = "Try a different filter or create a new note.",
                                    color     = TextHint,
                                    fontSize  = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                return@LazyColumn
            }

            // Note count label
            item {
                Text(
                    text     = "${notes.size} note${if (notes.size != 1) "s" else ""}",
                    color    = TextHint,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // Note cards
            items(notes, key = { it.id }) { note ->
                ProfessionalNoteCard(
                    note     = note,
                    onClick  = { onNoteClick(note) },
                    onDelete = { onDeleteNote(note.id) },
                    onGenerateQuiz = {
                        context.startActivity(
                            Intent(context, MockTestActivity::class.java).apply {
                                putExtra("noteId",    note.id)
                                putExtra("noteTitle", note.title)
                                putExtra("noteBody",  note.body)
                            }
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun NotesTopBar(onBackClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onBackClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint               = BrandPurple,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back", color = BrandPurple, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
private fun NotesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("Search notes…", color = TextHint, fontSize = 14.sp) },
        leadingIcon   = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextHint, modifier = Modifier.size(20.dp))
        },
        trailingIcon  = if (query.isNotBlank()) {
            { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextHint) } }
        } else null,
        singleLine      = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape           = RoundedCornerShape(12.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = BrandPurple,
            unfocusedBorderColor = Color(0xFFE0DDEF),
            cursorColor          = BrandPurple,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary
        )
    )
}

// ─── Source filter chips ──────────────────────────────────────────────────────

@Composable
private fun NoteSourceFilterChips(
    selected: SourceFilter,
    onSelect:  (SourceFilter) -> Unit,
    modifier:  Modifier = Modifier
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SourceFilter.entries.forEach { filter ->
            val isSelected = selected == filter
            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) BrandPurple else Color(0xFFF0EEF9),
                animationSpec = tween(180),
                label         = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) Color.White else TextSecondary,
                animationSpec = tween(180),
                label         = "chip_text"
            )
            Surface(
                shape    = RoundedCornerShape(50.dp),
                color    = bgColor,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Text(
                    text       = filter.label,
                    color      = textColor,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ─── Professional note card ───────────────────────────────────────────────────

@Composable
fun ProfessionalNoteCard(
    note:          NoteModel,
    onClick:       () -> Unit,
    onDelete:      () -> Unit,
    onGenerateQuiz: () -> Unit,
    modifier:      Modifier = Modifier
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStr = remember(note.updatedAt, note.timestamp) {
        val ts = if (note.updatedAt > 0L) note.updatedAt else note.timestamp
        if (ts == 0L) "" else dateFmt.format(Date(ts))
    }
    val words     = remember(note.body) { wordCount(note.body) }
    val quizReady = remember(note.body) { isQuizReady(note.body) }

    val (sourceLabel, sourceBg, sourceFg) = when (note.sourceType.uppercase()) {
        "VIDEO"    -> Triple("Video Note", Color(0xFFE8F5E9), Color(0xFF1B7A3E))
        "IMPORTED" -> Triple("Imported",   Color(0xFFFFF8E1), Color(0xFFC97B00))
        else       -> Triple("Manual",     StudyPurpleLight,  BrandPurple)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete Note", fontWeight = FontWeight.Bold) },
            text             = { Text("Delete \"${note.title.ifBlank { "Untitled" }}\"? This cannot be undone.") },
            confirmButton    = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors  = ButtonDefaults.buttonColors(containerColor = PriorityHigh)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.clickable(onClick = onClick).padding(16.dp)) {

            // Row 1: source badge + date
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = sourceBg) {
                    Text(
                        text       = sourceLabel,
                        color      = sourceFg,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (dateStr.isNotBlank()) {
                    Text(dateStr, color = TextHint, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text       = note.title.ifBlank { "Untitled Note" },
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            // Body preview
            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = note.body.replace("\n", " "),
                    color      = TextSecondary,
                    fontSize   = 13.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: word count + quiz readiness
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (words > 0) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0EEF9)) {
                        Text(
                            text     = "$words words",
                            color    = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                QuizReadinessBadge(isReady = quizReady)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 4: Generate Quiz + delete
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (quizReady) {
                    Button(
                        onClick          = onGenerateQuiz,
                        modifier         = Modifier.weight(1f).height(36.dp),
                        shape            = RoundedCornerShape(10.dp),
                        colors           = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                        contentPadding   = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Generate Quiz",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = Color(0xFFF5F5F5)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Needs more content",
                                fontSize = 12.sp,
                                color    = TextHint
                            )
                        }
                    }
                }
                IconButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint               = Color(0xFFCCCCCC),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Quiz readiness badge ─────────────────────────────────────────────────────

@Composable
fun QuizReadinessBadge(isReady: Boolean, modifier: Modifier = Modifier) {
    val bg    = if (isReady) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val fg    = if (isReady) Color(0xFF1B7A3E) else TextHint
    val label = if (isReady) "Quiz ready"     else "Too short"

    Surface(shape = RoundedCornerShape(6.dp), color = bg, modifier = modifier) {
        Row(
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun NotesEmptyState(
    onCreateNote:         () -> Unit,
    onNavigateVideoNotes: () -> Unit,
    modifier:             Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = StudyPurpleLight,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("📝", fontSize = 40.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text       = "No notes yet",
            color      = TextPrimary,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text      = "Create your first note or generate notes from a video to start building your study workspace.",
            color     = TextSecondary,
            fontSize  = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick  = onCreateNote,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Note", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick  = onNavigateVideoNotes,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
        ) {
            Text("Generate Video Notes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Note editor screen ───────────────────────────────────────────────────────

@Composable
fun NoteEditorScreen(
    existingNote:    NoteModel? = null,
    autoSaveStatus:  AutoSaveStatus = AutoSaveStatus.IDLE,
    onEditorChanged: (noteId: String?, title: String, body: String) -> Unit = { _, _, _ -> },
    onSave:          (title: String, body: String) -> Unit,
    onDelete:        ((noteId: String) -> Unit)? = null,
    onBack:          () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var body  by remember { mutableStateOf(existingNote?.body  ?: "") }

    val isEditing = existingNote != null
    val words     = remember(body) { wordCount(body) }
    val quizReady = remember(body) { isQuizReady(body) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(title, body) {
        if (title.isNotBlank() || body.isNotBlank()) {
            onEditorChanged(existingNote?.id, title, body)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete Note", fontWeight = FontWeight.Bold) },
            text             = { Text("Delete \"${existingNote?.title?.ifBlank { "Untitled" }}\"? This cannot be undone.") },
            confirmButton    = {
                Button(
                    onClick = { showDeleteDialog = false; existingNote?.let { onDelete?.invoke(it.id) } },
                    colors  = ButtonDefaults.buttonColors(containerColor = PriorityHigh)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // ── Top bar ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = BrandPurple,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Notes", color = BrandPurple, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val (statusText, statusColor) = when (autoSaveStatus) {
                        AutoSaveStatus.SAVING -> "Saving…" to TextHint
                        AutoSaveStatus.SAVED  -> "Saved"   to Color(0xFF1B7A3E)
                        AutoSaveStatus.FAILED -> "Failed"  to PriorityHigh
                        else                  -> ""        to Color.Transparent
                    }
                    if (statusText.isNotBlank()) {
                        Text(statusText, color = statusColor, fontSize = 12.sp)
                    }
                    Button(
                        onClick          = { onSave(title.trim(), body.trim()) },
                        enabled          = title.isNotBlank(),
                        shape            = RoundedCornerShape(10.dp),
                        contentPadding   = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                        modifier         = Modifier.height(36.dp),
                        colors           = ButtonDefaults.buttonColors(
                            containerColor         = BrandPurple,
                            disabledContainerColor = Color(0xFFD0CCEF)
                        )
                    ) {
                        Text(
                            text       = if (isEditing) "Update" else "Save",
                            color      = Color.White,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEAFF))

        // ── Editor canvas ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Source type badge (readonly)
            Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight) {
                Text(
                    text       = when (existingNote?.sourceType?.uppercase()) {
                        "VIDEO"    -> "Video Note"
                        "IMPORTED" -> "Imported"
                        else       -> "Manual Note"
                    },
                    color      = BrandPurple,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title field
            EditorTextField(
                value       = title,
                onChange    = { title = it },
                placeholder = "Untitled Note",
                textStyle   = TextStyle(
                    color      = TextPrimary,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0EEF9))
            Spacer(modifier = Modifier.height(14.dp))

            // Body field
            EditorTextField(
                value       = body,
                onChange    = { body = it },
                placeholder = "Start writing your notes here…\n\nTip: Write definitions, concepts, and explanations — at least 50 words — to unlock quiz generation.",
                textStyle   = TextStyle(
                    color      = TextPrimary,
                    fontSize   = 16.sp,
                    lineHeight = 26.sp
                ),
                minLines    = 14
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Bottom action bar ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalDivider(color = Color(0xFFEEEAFF))
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("$words words", color = TextHint, fontSize = 12.sp)
                QuizReadinessBadge(isReady = quizReady)
            }

            if (isEditing && existingNote != null) {
                val context = LocalContext.current
                if (quizReady) {
                    Button(
                        onClick  = {
                            context.startActivity(
                                Intent(context, MockTestActivity::class.java).apply {
                                    putExtra("noteId",    existingNote.id)
                                    putExtra("noteTitle", title.ifBlank { existingNote.title })
                                    putExtra("noteBody",  body.ifBlank { existingNote.body })
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Text("Generate Quiz", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        color    = Color(0xFFF5F5F5)
                    ) {
                        Text(
                            text      = "This note needs more content before generating a quiz.",
                            color     = TextHint,
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (isEditing && onDelete != null) {
                TextButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint               = PriorityHigh,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Delete Note",
                        color      = PriorityHigh,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 14.sp
                    )
                }
            }
        }
    }
}

// ─── Editor text field helper ─────────────────────────────────────────────────

@Composable
private fun EditorTextField(
    value:       String,
    onChange:    (String) -> Unit,
    placeholder: String,
    textStyle:   TextStyle,
    minLines:    Int = 1
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (value.isEmpty()) {
            Text(
                text     = placeholder,
                style    = textStyle.copy(color = TextHint),
                modifier = Modifier.fillMaxWidth()
            )
        }
        BasicTextField(
            value          = value,
            onValueChange  = onChange,
            textStyle      = textStyle,
            cursorBrush    = SolidColor(BrandPurple),
            minLines       = minLines,
            modifier       = Modifier.fillMaxWidth()
        )
    }
}
