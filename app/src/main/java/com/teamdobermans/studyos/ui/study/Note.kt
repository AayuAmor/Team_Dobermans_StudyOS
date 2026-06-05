package com.teamdobermans.studyos.ui.study
import com.teamdobermans.studyos.R

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.Task
import com.teamdobermans.studyos.ui.plan.PlanActivity
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.NoteViewModel

private val BrandPurpleLight = Color(0xFF7C6CEF)
private val BackgroundGray = StudyPurpleFaint
private val SelectedChipBg = Color(0xFFDED9FF)

class NotesPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesScreen(onBackClick = { finish() })
                }
            }
        }
    }
}

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel? = null,
    onBackClick: () -> Unit = {}
) {
    val resolvedVm: NoteViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    val allNotes    by resolvedVm.notes.collectAsState()
    val saveResult  by resolvedVm.saveResult.collectAsState()
    val linkedTasks by resolvedVm.linkedTasks.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveResult) {
        saveResult?.let { result ->
            val message = if (result == "SUCCESS") "Note saved!" else result
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            resolvedVm.clearSaveResult()
        }
    }

    NotesScreenContent(
        modifier        = modifier,
        allNotes        = allNotes,
        linkedTasks     = linkedTasks,
        onBackClick     = onBackClick,
        onNoteSelected  = { noteId -> resolvedVm.loadLinkedTasks(noteId) },
        onNoteDeselected = { resolvedVm.clearLinkedTasks() },
        onCreateNote    = { title, body, folder -> resolvedVm.createNote(title, body, folder) },
        onUpdateNote    = { note -> resolvedVm.updateNote(note) },
        onDeleteNote    = { id -> resolvedVm.deleteNote(id) }
    )
}

@Composable
fun NotesScreenContent(
    modifier: Modifier = Modifier,
    allNotes: List<NoteModel>,
    linkedTasks: List<Task> = emptyList(),
    onBackClick: () -> Unit,
    onNoteSelected: (String) -> Unit = {},
    onNoteDeselected: () -> Unit = {},
    onCreateNote: (String, String, String) -> Unit,
    onUpdateNote: (NoteModel) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    val defaultFolders = listOf("Science", "Social", "English")
    var extraFolders by rememberSaveable { mutableStateOf(listOf<String>()) }
    val folders = defaultFolders + extraFolders

    var activeFolder        by rememberSaveable { mutableStateOf("Science") }
    var searchQuery         by rememberSaveable { mutableStateOf("") }
    var selectedNote        by remember { mutableStateOf<NoteModel?>(null) }
    var showEditor          by rememberSaveable { mutableStateOf(false) }
    var showNewFolderDialog by rememberSaveable { mutableStateOf(false) }
    var newFolderName       by rememberSaveable { mutableStateOf("") }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false; newFolderName = "" },
            title = { Text("New Folder", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder name") },
                    placeholder = { Text("e.g. Computer, Math...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        focusedLabelColor  = BrandPurple,
                        cursorColor        = BrandPurple
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isNotEmpty() && !folders.contains(name)) {
                            extraFolders = extraFolders + name
                            activeFolder = name
                        }
                        showNewFolderDialog = false
                        newFolderName = ""
                    },
                    enabled = newFolderName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) { Text("Create", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false; newFolderName = "" }) { Text("Cancel") }
            }
        )
    }

    if (showEditor) {
        CreateEditNoteScreen(
            existingNote  = selectedNote,
            folders       = folders,
            defaultFolder = activeFolder,
            linkedTasks   = linkedTasks,
            onSave = { title, body, folder ->
                if (selectedNote != null) {
                    onUpdateNote(selectedNote!!.copy(title = title, body = body, folder = folder))
                } else {
                    onCreateNote(title, body, folder)
                }
                showEditor = false
                selectedNote = null
                onNoteDeselected()
            },
            onDelete = { noteId ->
                onDeleteNote(noteId)
                showEditor = false
                selectedNote = null
                onNoteDeselected()
            },
            onBack = {
                showEditor = false
                selectedNote = null
                onNoteDeselected()
            }
        )
    } else {
        val visibleNotes = if (searchQuery.isNotEmpty()) {
            allNotes.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) ||
                        note.body.contains(searchQuery, ignoreCase = true)
            }
        } else {
            allNotes.filter { note -> note.folder == activeFolder }
        }

        Scaffold(
            modifier       = modifier.fillMaxSize(),
            containerColor = BackgroundGray,
            floatingActionButton = {
                FloatingActionButton(
                    onClick        = { selectedNote = null; showEditor = true },
                    modifier       = Modifier.padding(bottom = 16.dp),
                    containerColor = BrandPurple,
                    shape          = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NotesHeader(
                    searchQuery    = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onBackClick    = onBackClick,
                    onAddClick     = { showNewFolderDialog = true }
                )

                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        item {
                            FoldersSection(
                                folders      = folders,
                                activeFolder = activeFolder,
                                onFolderClick = { activeFolder = it },
                                onNewFolder   = { showNewFolderDialog = true }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Results for \"$searchQuery\"",
                                color = BrandPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    items(visibleNotes, key = { it.id }) { note ->
                        NoteCard(
                            note    = note,
                            onClick = {
                                selectedNote = note
                                showEditor = true
                                if (note.id.isNotBlank()) onNoteSelected(note.id)
                            },
                            onClose = { onDeleteNote(note.id) }
                        )
                    }

                    if (visibleNotes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (searchQuery.isNotEmpty())
                                            "No notes found for \"$searchQuery\""
                                        else
                                            "No notes in $activeFolder yet.",
                                        color = Color.Gray,
                                        fontSize = 15.sp
                                    )
                                    if (searchQuery.isEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tap the purple + button to write one!",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEditNoteScreen(
    existingNote: NoteModel? = null,
    folders: List<String> = listOf("Science", "Social", "English"),
    defaultFolder: String = "Science",
    linkedTasks: List<Task> = emptyList(),
    onSave: (title: String, body: String, folder: String) -> Unit,
    onDelete: ((noteId: String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var body by remember { mutableStateOf(existingNote?.body ?: "") }
    var folder by remember { mutableStateOf(existingNote?.folder ?: defaultFolder) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditing = existingNote != null
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text  = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    existingNote?.let { onDelete?.invoke(it.id) }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray).imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudyPurpleDeep)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(BrandPurpleLight)
                        .clickable { onBack() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Text(
                    text = if (isEditing) "Edit Note" else "New Note",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Folder", color = BrandPurple, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                folders.forEach { f ->
                    FolderChip(label = f, isActive = f == folder, onClick = { folder = f })
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    focusedLabelColor  = BrandPurple,
                    cursorColor        = BrandPurple
                )
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                maxLines = Int.MAX_VALUE,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    focusedLabelColor  = BrandPurple,
                    cursorColor        = BrandPurple
                )
            )

            // ── Linked Tasks section (only for existing notes) ───────────────
            if (isEditing) {
                LinkedTasksSection(
                    linkedTasks = linkedTasks,
                    onOpenTask  = {
                        context.startActivity(Intent(context, PlanActivity::class.java))
                    }
                )
            }

            Button(
                onClick  = { onSave(title.trim(), body.trim(), folder) },
                enabled  = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = BrandPurple,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Update Note" else "Save Note",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isEditing && onDelete != null) {
                Button(
                    onClick  = { showDeleteDialog = true },
                    colors   = ButtonDefaults.buttonColors(containerColor = PriorityHigh),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Note", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LinkedTasksSection(
    linkedTasks: List<Task>,
    onOpenTask: (Task) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = StudyPurpleLight
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Linked Tasks",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (linkedTasks.isEmpty()) {
                Text(
                    "No tasks linked to this note",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            } else {
                linkedTasks.forEach { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onOpenTask(task) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("•", color = BrandPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    task.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = when (task.priority) {
                                    com.teamdobermans.studyos.model.Priority.HIGH   -> Color(0xFFFFEBEB)
                                    com.teamdobermans.studyos.model.Priority.MEDIUM -> Color(0xFFFFF8E1)
                                    com.teamdobermans.studyos.model.Priority.LOW    -> Color(0xFFE8F5E9)
                                }
                            ) {
                                Text(
                                    task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = when (task.priority) {
                                        com.teamdobermans.studyos.model.Priority.HIGH   -> Color(0xFFE53935)
                                        com.teamdobermans.studyos.model.Priority.MEDIUM -> Color(0xFFF57F17)
                                        com.teamdobermans.studyos.model.Priority.LOW    -> Color(0xFF2E7D32)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPurple)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(BrandPurpleLight)
                    .clickable { onBackClick() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Notes", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)),
            placeholder = { Text("Search notes...", color = Color(0xFF9988CC), fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    tint = Color(0xFF9988CC),
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.White.copy(alpha = 0.15f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor             = Color.White,
                focusedTextColor        = Color.White,
                unfocusedTextColor      = Color.White
            )
        )
    }
}

@Composable
fun FoldersSection(
    folders: List<String>,
    activeFolder: String,
    onFolderClick: (String) -> Unit,
    onNewFolder: () -> Unit = {}
) {
    Column {
        Text(
            text = "Folders",
            color = BrandPurple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            folders.forEach { folder ->
                FolderChip(label = folder, isActive = folder == activeFolder, onClick = { onFolderClick(folder) })
            }
            FolderChip(label = "+ New Folder", isActive = false, onClick = onNewFolder)
        }
    }
}

@Composable
fun FolderChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    val chipModifier = if (isActive) {
        Modifier.shadow(4.dp, RoundedCornerShape(10.dp))
    } else {
        Modifier
    }
    Box(
        modifier = chipModifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) BrandPurple else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else TextSecondary,
            fontSize = 15.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun NoteCard(note: NoteModel, onClick: () -> Unit, onClose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    color = BrandPurple,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = "Delete",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp).clickable { onClose() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = note.body, color = Color.DarkGray, fontSize = 15.sp, lineHeight = 22.sp, maxLines = 3)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = note.folder, color = BrandPurple.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    StudyOSTheme {
        NotesScreenContent(
            allNotes = listOf(
                NoteModel(id = "1", title = "Periodic Table", body = "Elements are organized by atomic number.", folder = "Science"),
                NoteModel(id = "2", title = "Computer", body = "A computer is an electronic device.", folder = "Computer")
            ),
            onBackClick  = {},
            onCreateNote = { _, _, _ -> },
            onUpdateNote = {},
            onDeleteNote = {}
        )
    }
}