package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme

data class Note(
    val id: Int,
    val title: String,
    val body: String,
    val folder: String
)

data class NavTab(
    val label: String,
    val iconResId: Int
)

private val BrandPurple = Color(0xFF6C5CE7)
private val BrandPurpleLight = Color(0xFF7C6CEF)
private val BackgroundGray = Color(0xFFF0EFF5)
private val SelectedChipBg = Color(0xFFDED9FF)

class NotesPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onNoteClick: (Note) -> Unit = {},
    onNavClick: (String) -> Unit = {}
) {
    var activeFolder by remember { mutableStateOf("Science") }
    var searchQuery by remember { mutableStateOf("") }
    var activeNavTab by remember { mutableStateOf("Study") }

    val folders = listOf(
        stringResource(R.string.folder_science),
        stringResource(R.string.folder_social),
        stringResource(R.string.folder_english)
    )

    val sampleNotes = listOf(
        Note(
            1,
            stringResource(R.string.note_periodic_table_title),
            stringResource(R.string.note_periodic_table_body),
            stringResource(R.string.folder_science)
        ),
        Note(
            2,
            stringResource(R.string.note_mitochondria_title),
            stringResource(R.string.note_mitochondria_body),
            stringResource(R.string.folder_science)
        )
    )

    var visibleNotes by remember(activeFolder, searchQuery) {
        mutableStateOf(
            sampleNotes.filter { note ->
                note.folder == activeFolder &&
                        (searchQuery.isEmpty() ||
                                note.title.contains(searchQuery, ignoreCase = true) ||
                                note.body.contains(searchQuery, ignoreCase = true))
            }.toMutableList()
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = BackgroundGray,
        bottomBar = {
            BottomNavBar(
                activeTab = activeNavTab,
                onTabClick = { tab ->
                    activeNavTab = tab
                    onNavClick(tab)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NotesHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBackClick = onBackClick,
                onAddClick = onAddClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FoldersSection(
                        folders = folders,
                        activeFolder = activeFolder,
                        onFolderClick = { activeFolder = it }
                    )
                }

                items(visibleNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onClose = {
                            visibleNotes = visibleNotes.toMutableList().also { it.remove(note) }
                        }
                    )
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
                    Text(
                        text = stringResource(R.string.back),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.notes_title),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp)),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    color = Color(0xFF9988CC),
                    fontSize = 14.sp
                )
            },
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
                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
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
            text = stringResource(R.string.folders_title),
            color = BrandPurple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            folders.forEach { folder ->
                FolderChip(
                    label = folder,
                    isActive = folder == activeFolder,
                    onClick = { onFolderClick(folder) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FolderChip(
            label = stringResource(R.string.new_folder),
            isActive = false,
            onClick = onNewFolder
        )
    }
}

@Composable
fun FolderChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) SelectedChipBg else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) BrandPurple else Color.DarkGray,
            fontSize = 15.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = "Close",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = note.body,
                color = Color.DarkGray,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun BottomNavBar(
    activeTab: String,
    onTabClick: (String) -> Unit
) {
    val tabs = listOf(
        NavTab(stringResource(R.string.nav_home), R.drawable.baseline_home_24),
        NavTab(stringResource(R.string.nav_study), R.drawable.baseline_menu_book_24),
        NavTab(stringResource(R.string.nav_plan), R.drawable.ic_calendar),
        NavTab(stringResource(R.string.nav_progress), R.drawable.ic_progress),
        NavTab(stringResource(R.string.nav_settings), R.drawable.baseline_settings_24)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = tab.label == activeTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) SelectedChipBg else Color.Transparent)
                        .clickable { onTabClick(tab.label) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = tab.iconResId),
                            contentDescription = tab.label,
                            tint = if (isActive) BrandPurple else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = tab.label,
                            color = if (isActive) BrandPurple else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    StudyOSTheme {
        NotesScreen()
    }
}
