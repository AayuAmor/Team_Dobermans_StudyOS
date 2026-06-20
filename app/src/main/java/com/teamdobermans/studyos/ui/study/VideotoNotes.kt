package com.teamdobermans.studyos.ui.study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.NotesOutput
import com.teamdobermans.studyos.model.VideoNotesResponse
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextHint
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.SaveState
import com.teamdobermans.studyos.viewModel.VideoNotesUiState
import com.teamdobermans.studyos.viewModel.VideoNotesViewModel

class VideotoNotes : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyOSTheme {
                VideoToNotesScreen(onBack = { finish() })
            }
        }
    }
}

private enum class InputMode { YOUTUBE, UPLOAD }

private enum class SummaryStyle(val label: String) {
    SHORT("Short Summary"),
    DETAILED("Detailed Notes"),
    EXAM("Exam Notes"),
    BULLETS("Bullet Points")
}

@Composable
fun VideoToNotesScreen(
    onBack: () -> Unit = {},
    vm: VideoNotesViewModel = viewModel()
) {
    val uiState  by vm.uiState.collectAsState()
    val saveState by vm.saveState.collectAsState()

    var inputMode        by remember { mutableStateOf(InputMode.YOUTUBE) }
    var youtubeUrl       by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var summaryStyle     by remember { mutableStateOf(SummaryStyle.SHORT) }

    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

            val currentSummaryStyle by rememberUpdatedState(summaryStyle)

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFileName = uri.lastPathSegment ?: "video"
            vm.generateFromUpload(uri, currentSummaryStyle.name.lowercase())
        }
    }

        LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Saved       -> {
                snackbarHostState.showSnackbar("Note saved successfully!")
                vm.resetSaveState()
            }
            is SaveState.Error       -> {
                snackbarHostState.showSnackbar((saveState as SaveState.Error).message)
                vm.resetSaveState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
                        item { VideoNotesHeader(onBack = onBack) }

                        item {
                Spacer(Modifier.height(20.dp))
                InputModeTabRow(
                    selected = inputMode,
                    onSelect = {
                        inputMode = it
                        vm.reset()
                        selectedFileName = null
                    }
                )
            }

                        item {
                Spacer(Modifier.height(16.dp))
                AnimatedContent(targetState = inputMode, label = "input_mode") { mode ->
                    when (mode) {
                        InputMode.YOUTUBE -> YoutubeInputCard(
                            url         = youtubeUrl,
                            onUrlChange = { youtubeUrl = it }
                        )
                        InputMode.UPLOAD  -> UploadInputCard(
                            selectedFileName = selectedFileName,
                            onPickFile       = { fileLauncher.launch("video/*") }
                        )
                    }
                }
            }

                        if (uiState !is VideoNotesUiState.Loading) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SummaryStylePicker(selected = summaryStyle, onSelect = { summaryStyle = it })
                }
            }

                        item {
                Spacer(Modifier.height(20.dp))
                val canGenerate = when (inputMode) {
                    InputMode.YOUTUBE -> youtubeUrl.isNotBlank() && uiState !is VideoNotesUiState.Loading
                    InputMode.UPLOAD  -> false                 }
                if (inputMode == InputMode.YOUTUBE) {
                    GenerateButton(
                        enabled      = canGenerate,
                        isLoading    = uiState is VideoNotesUiState.Loading,
                        onClick      = { vm.generateFromUrl(youtubeUrl, summaryStyle.name.lowercase()) }
                    )
                }
            }

                        if (inputMode == InputMode.UPLOAD && uiState is VideoNotesUiState.Loading) {
                item {
                    Spacer(Modifier.height(20.dp))
                    UploadLoadingCard(selectedFileName = selectedFileName)
                }
            }

                        item {
                AnimatedVisibility(
                    visible = uiState is VideoNotesUiState.Error,
                    enter   = fadeIn() + slideInVertically { it / 2 },
                    exit    = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    val msg = (uiState as? VideoNotesUiState.Error)?.message ?: ""
                    Column {
                        Spacer(Modifier.height(20.dp))
                        ErrorCard(message = msg, onRetry = { vm.reset() })
                    }
                }
            }

                        item {
                AnimatedVisibility(
                    visible = uiState is VideoNotesUiState.Success,
                    enter   = fadeIn() + slideInVertically { it / 2 },
                    exit    = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    val response = (uiState as? VideoNotesUiState.Success)?.response
                    if (response != null) {
                        Column {
                            Spacer(Modifier.height(24.dp))
                            OutputCard(
                                response      = response,
                                saveState     = saveState,
                                onSaveToNotes = {
                                    val body = buildNoteBody(response.notes)
                                    vm.saveToNotes(response.title, body)
                                },
                                onCopy        = {
                                    clipboard.setText(
                                        AnnotatedString(buildNoteBody(response.notes))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun VideoNotesHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Video to Notes",
                        color      = Color.White,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "AI-powered video summarization",
                        color    = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint               = Color(0xFFFFD54F),
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Paste a YouTube link or upload a video to get structured study notes instantly.",
                        color      = Color.White.copy(alpha = 0.85f),
                        fontSize   = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InputModeTabRow(
    selected: InputMode,
    onSelect: (InputMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudyPurpleLight)
            .padding(4.dp)
    ) {
        InputMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val label = if (mode == InputMode.YOUTUBE) "YouTube Link" else "Upload Video"
            val icon  = if (mode == InputMode.YOUTUBE) Icons.Default.InsertLink else Icons.Default.CloudUpload
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(mode) },
                color    = if (isSelected) StudyPurple else Color.Transparent,
                shape    = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier              = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = if (isSelected) Color.White else TextSecondary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label,
                        color      = if (isSelected) Color.White else TextSecondary,
                        fontSize   = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun YoutubeInputCard(url: String, onUrlChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudyPurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.InsertLink, null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("YouTube Link", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("Paste a YouTube video URL", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = url,
                onValueChange = onUrlChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("https://youtube.com/watch?v=...", color = TextHint, fontSize = 13.sp) },
                leadingIcon   = {
                    Icon(Icons.Default.InsertLink, null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                },
                trailingIcon  = if (url.isNotBlank()) {
                    { IconButton(onClick = { onUrlChange("") }) {
                        Icon(Icons.Default.ContentCopy, "Clear", tint = TextHint, modifier = Modifier.size(18.dp))
                    }}
                } else null,
                singleLine = true,
                shape      = RoundedCornerShape(12.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = StudyPurple,
                    unfocusedBorderColor    = StudyPurpleLight,
                    focusedContainerColor   = StudyPurpleFaint,
                    unfocusedContainerColor = StudyPurpleFaint
                )
            )
            if (url.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("URL ready — tap Generate Notes", color = Color(0xFF4CAF50), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun UploadInputCard(selectedFileName: String?, onPickFile: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudyPurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudUpload, null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Upload Video", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("MP4, MOV, MKV, MP3, WAV supported", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (selectedFileName != null) StudyPurple else StudyPurpleLight,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(if (selectedFileName != null) StudyPurpleLight else StudyPurpleFaint)
                    .clickable { onPickFile() }
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = if (selectedFileName != null) Icons.Default.FilePresent else Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint               = StudyPurple,
                        modifier           = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    if (selectedFileName != null) {
                        Text(selectedFileName, color = StudyPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap to change file", color = TextHint, fontSize = 11.sp)
                    } else {
                        Text("Choose video from device", color = StudyPurple, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap to browse files", color = TextHint, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadLoadingCard(selectedFileName: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = StudyPurple, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(14.dp))
            Text("Transcribing video…", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            if (selectedFileName != null) {
                Spacer(Modifier.height(4.dp))
                Text(selectedFileName, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "This may take a few minutes depending on video length.",
                color     = TextHint,
                fontSize  = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummaryStylePicker(selected: SummaryStyle, onSelect: (SummaryStyle) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Summary Style", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Choose how your notes are structured", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        val styles = SummaryStyle.entries
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            styles.take(2).forEach { style ->
                StyleChip(style = style, isSelected = style == selected, onSelect = onSelect, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            styles.drop(2).forEach { style ->
                StyleChip(style = style, isSelected = style == selected, onSelect = onSelect, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StyleChip(
    style: SummaryStyle,
    isSelected: Boolean,
    onSelect: (SummaryStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier        = modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelect(style) },
        shape           = RoundedCornerShape(12.dp),
        color           = if (isSelected) StudyPurple else Color.White,
        shadowElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint               = if (isSelected) Color.White else StudyPurple,
                modifier           = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                style.label,
                color      = if (isSelected) Color.White else TextPrimary,
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GenerateButton(enabled: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick   = onClick,
        enabled   = enabled,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(54.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor         = StudyPurple,
            disabledContainerColor = StudyPurpleLight
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Analyzing video…", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        } else {
            Icon(Icons.Default.AutoAwesome, null, tint = if (enabled) Color.White else TextHint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Generate Notes", color = if (enabled) Color.White else TextHint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F1))
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint               = Color(0xFFE53935),
                modifier           = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text("Something went wrong", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB71C1C))
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                color     = Color(0xFFE53935),
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onRetry,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Try Again", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OutputCard(
    response: VideoNotesResponse,
    saveState: SaveState,
    onSaveToNotes: () -> Unit,
    onCopy: () -> Unit
) {
    val notes = response.notes
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(StudyPurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = StudyPurple, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = response.title.ifBlank { "Generated Notes" },
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = TextPrimary,
                        maxLines   = 2
                    )
                    Text("AI summary ready", fontSize = 12.sp, color = Color(0xFF4CAF50))
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Done", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

                        if (notes.summary.isNotBlank()) {
                Divider()
                SectionTitle("Summary")
                Spacer(Modifier.height(8.dp))
                Text(notes.summary, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
            }

                        if (notes.keyPoints.isNotEmpty()) {
                Divider()
                SectionTitle("Key Points")
                Spacer(Modifier.height(10.dp))
                notes.keyPoints.forEach { point ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StudyPurple).align(Alignment.CenterVertically))
                        Spacer(Modifier.width(10.dp))
                        Text(point, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }

                        if (notes.studyNotes.isNotEmpty()) {
                Divider()
                SectionTitle("Study Notes")
                Spacer(Modifier.height(10.dp))
                notes.studyNotes.forEach { section ->
                    Text(section.heading, color = StudyPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    section.points.forEach { point ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 6.dp), verticalAlignment = Alignment.Top) {
                            Text("•", color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(point, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

                        if (notes.importantTerms.isNotEmpty()) {
                Divider()
                SectionTitle("Important Terms")
                Spacer(Modifier.height(10.dp))
                notes.importantTerms.forEach { term ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = StudyPurpleFaint
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(term.term, color = StudyPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(term.meaning, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            }

                        if (notes.possibleQuestions.isNotEmpty()) {
                Divider()
                SectionTitle("Possible Exam Questions")
                Spacer(Modifier.height(10.dp))
                notes.possibleQuestions.forEachIndexed { i, q ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Text("${i + 1}.", color = StudyPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(q, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }

                        Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    label     = when (saveState) {
                        is SaveState.Saving -> "Saving…"
                        is SaveState.Saved  -> "Saved!"
                        else                -> "Save to Notes"
                    },
                    icon      = if (saveState is SaveState.Saved) Icons.Default.CheckCircle else Icons.Default.Save,
                    isPrimary = true,
                    enabled   = saveState !is SaveState.Saving && saveState !is SaveState.Saved,
                    onClick   = onSaveToNotes,
                    modifier  = Modifier.weight(1f)
                )
                ActionButton(
                    label    = "Copy",
                    icon     = Icons.Default.ContentCopy,
                    onClick  = onCopy,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = StudyPurpleLight, thickness = 1.dp)
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        modifier        = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        shape           = RoundedCornerShape(12.dp),
        color           = when {
            !enabled && isPrimary -> StudyPurpleLight
            isPrimary             -> StudyPurple
            else                  -> StudyPurpleLight
        }
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = if (isPrimary && enabled) Color.White else StudyPurple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = if (isPrimary && enabled) Color.White else StudyPurple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun buildNoteBody(notes: NotesOutput): String = buildString {
    if (notes.summary.isNotBlank()) {
        appendLine("## Summary")
        appendLine(notes.summary)
        appendLine()
    }
    if (notes.keyPoints.isNotEmpty()) {
        appendLine("## Key Points")
        notes.keyPoints.forEach { appendLine("• $it") }
        appendLine()
    }
    if (notes.studyNotes.isNotEmpty()) {
        appendLine("## Study Notes")
        notes.studyNotes.forEach { section ->
            appendLine("### ${section.heading}")
            section.points.forEach { appendLine("• $it") }
            appendLine()
        }
    }
    if (notes.importantTerms.isNotEmpty()) {
        appendLine("## Important Terms")
        notes.importantTerms.forEach { appendLine("**${it.term}**: ${it.meaning}") }
        appendLine()
    }
    if (notes.possibleQuestions.isNotEmpty()) {
        appendLine("## Possible Exam Questions")
        notes.possibleQuestions.forEachIndexed { i, q -> appendLine("${i + 1}. $q") }
    }
}
