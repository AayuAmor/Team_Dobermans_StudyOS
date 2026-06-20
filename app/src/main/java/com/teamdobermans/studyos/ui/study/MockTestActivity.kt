package com.teamdobermans.studyos.ui.study
import com.teamdobermans.studyos.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.model.Difficulty
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.NoteReadinessInfo
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizQuestionModel
import com.teamdobermans.studyos.model.QuizSetupUiState
import com.teamdobermans.studyos.model.QuizTypeOption
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.MockTestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QuizGradient    = listOf(Color(0xFF4A3EC8), Color(0xFF6A5AF9))
private val CorrectGreen    = Color(0xFF1B7A3E)
private val CorrectGreenBg  = Color(0xFFE8F5E9)
private val WrongRed        = Color(0xFFE53935)
private val WrongRedBg      = Color(0xFFFFF0F0)
private val WarningOrange   = Color(0xFFC97B00)
private val WarningOrangeBg = Color(0xFFFFF8E1)

class MockTestActivity : ComponentActivity() {
    private val vm: MockTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteId    = intent.getStringExtra("noteId")
        val noteTitle = intent.getStringExtra("noteTitle") ?: ""
        val noteBody  = intent.getStringExtra("noteBody") ?: ""
        if (noteId != null) {
            vm.preSelectNote(noteId, noteTitle, noteBody)
        }

        onBackPressedDispatcher.addCallback(this) {
            when (vm.uiState.value) {
                QuizSetupUiState.NoteSelection -> finish()
                is QuizSetupUiState.Setup      -> vm.navigateToNoteSelection()
                is QuizSetupUiState.Generating -> { }
                else                           -> vm.backToSetup()
            }
        }

        setContent {
            StudyOSTheme {
                QuizSetupBody(vm = vm, onBack = { finish() })
            }
        }
    }
}

@Composable
fun QuizSetupBody(
    vm: MockTestViewModel,
    onBack: () -> Unit = {}
) {
    val uiState       by vm.uiState.collectAsState()
    val notes         by vm.notes.collectAsState()
    val notesLoading  by vm.notesLoading.collectAsState()
    val history       by vm.history.collectAsState()
    val questionCount by vm.questionCount.collectAsState()
    val difficulty    by vm.difficulty.collectAsState()
    val questionType  by vm.questionType.collectAsState()

    when (val state = uiState) {
        QuizSetupUiState.NoteSelection -> {
            NoteSelectionScreen(
                notes        = notes,
                isLoading    = notesLoading,
                onSelectNote = { note -> vm.selectNote(note) },
                onBack       = onBack
            )
        }
        is QuizSetupUiState.Setup -> {
            QuizSetupScreen(
                state              = state,
                questionCount      = questionCount,
                difficulty         = difficulty,
                questionType       = questionType,
                hasHistory         = history.isNotEmpty(),
                onCountChange      = { vm.setQuestionCount(it) },
                onDifficultyChange = { vm.setDifficulty(it) },
                onTypeChange       = { vm.setQuestionType(it) },
                onGenerate         = { vm.generateQuiz() },
                onChangeNote       = { vm.navigateToNoteSelection() },
                onShowHistory      = { vm.showHistory() },
                onBack             = { vm.navigateToNoteSelection() }
            )
        }
        is QuizSetupUiState.Generating -> {
            GeneratingScreen(note = state.note, onBack = onBack)
        }
        is QuizSetupUiState.Playing -> {
            QuizPlayingScreen(
                state      = state,
                onAnswer   = { idx, ans -> vm.selectAnswer(idx, ans) },
                onNext     = { vm.nextQuestion() },
                onPrevious = { vm.previousQuestion() },
                onSubmit   = { vm.submitQuiz() },
                onBack     = { vm.backToSetup() }
            )
        }
        is QuizSetupUiState.Result -> {
            QuizResultScreen(
                state     = state,
                onRetake  = { vm.retakeQuiz() },
                onNewQuiz = { vm.backToSetup() },
                onHistory = { vm.showHistory() },
                onBack    = { vm.backToSetup() }
            )
        }
        QuizSetupUiState.History -> {
            MockHistoryScreen(
                history = history,
                onBack  = { vm.backToSetup() }
            )
        }
        is QuizSetupUiState.Error -> {
            QuizErrorScreen(
                message = state.message,
                onBack  = { vm.backToSetup() }
            )
        }
    }
}

@Composable
private fun NoteSelectionScreen(
    notes: List<NoteModel>,
    isLoading: Boolean,
    onSelectNote: (NoteModel) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                MockBackButton(onClick = onBack)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Create Quiz", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Select a note to quiz yourself on",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = StudyPurple)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading your notes…", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
            notes.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier            = Modifier.padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No notes yet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create or import notes to generate quizzes.",
                            color     = TextSecondary,
                            fontSize  = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text       = "${notes.size} note${if (notes.size != 1) "s" else ""} available",
                            color      = TextHint,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    items(notes, key = { it.id }) { note ->
                        NoteSelectCard(note = note, onClick = { onSelectNote(note) })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NoteSelectCard(note: NoteModel, onClick: () -> Unit) {
    val dateFmt  = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateStr  = remember(note.timestamp) {
        if (note.timestamp == 0L) "" else dateFmt.format(Date(note.timestamp))
    }
    val wordCount = remember(note.body) {
        note.body.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val sourceLabel = when (note.sourceType) {
        "VIDEO"    -> "Video Note"
        "IMPORTED" -> "Imported"
        else       -> "Manual Note"
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight) {
                    Text(
                        text       = sourceLabel,
                        color      = StudyPurple,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (dateStr.isNotBlank()) {
                    Text(text = dateStr, color = TextHint, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = note.title.ifBlank { "Untitled Note" },
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            if (note.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = note.body.replace("\n", " "),
                    color      = TextSecondary,
                    fontSize   = 13.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight) {
                    Text(
                        text     = note.folder,
                        color    = StudyPurple,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0F0F0)) {
                    Text(
                        text     = "$wordCount words",
                        color    = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizSetupScreen(
    state: QuizSetupUiState.Setup,
    questionCount: Int,
    difficulty: Difficulty,
    questionType: QuizTypeOption,
    hasHistory: Boolean,
    onCountChange: (Int) -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onTypeChange: (QuizTypeOption) -> Unit,
    onGenerate: () -> Unit,
    onChangeNote: () -> Unit,
    onShowHistory: () -> Unit,
    onBack: () -> Unit
) {
    val note      = state.note
    val readiness = state.readiness
    val wordCount = readiness.wordCount

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    MockBackButton(onClick = onBack)
                    if (hasHistory) {
                        IconButton(onClick = onShowHistory) {
                            Icon(
                                imageVector        = Icons.Default.History,
                                contentDescription = "Quiz History",
                                tint               = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Quiz Setup", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Configure your quiz for this note",
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = note.title.ifBlank { "Untitled Note" },
                                color      = TextPrimary,
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines   = 2,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight) {
                                    Text(
                                        text     = note.folder,
                                        color    = StudyPurple,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0F0F0)) {
                                    Text(
                                        text     = "$wordCount words",
                                        color    = TextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (note.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text       = note.body.replace("\n", " "),
                            color      = TextSecondary,
                            fontSize   = 13.sp,
                            maxLines   = 3,
                            overflow   = TextOverflow.Ellipsis,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ReadinessCard(readiness = readiness)
                }
            }

            if (!readiness.hasEnoughContent) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = WrongRedBg)
                ) {
                    Row(
                        modifier          = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚠", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Insufficient Content",
                                color      = WrongRed,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "This note does not contain enough content to generate a reliable quiz. " +
                                "Add definitions, explanations, and descriptions to enable quiz generation.",
                                color      = WrongRed.copy(alpha = 0.85f),
                                fontSize   = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            ConfigSection(label = "QUESTION COUNT") {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        CountChip(
                            count    = count,
                            selected = questionCount == count,
                            modifier = Modifier.weight(1f),
                            onClick  = { onCountChange(count) }
                        )
                    }
                }
            }

            ConfigSection(label = "DIFFICULTY") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DifficultyChip(
                        label         = "Easy",
                        selected      = difficulty == Difficulty.EASY,
                        selectedColor = CorrectGreen,
                        bgColor       = CorrectGreenBg,
                        modifier      = Modifier.weight(1f)
                    ) { onDifficultyChange(Difficulty.EASY) }
                    DifficultyChip(
                        label         = "Medium",
                        selected      = difficulty == Difficulty.MEDIUM,
                        selectedColor = WarningOrange,
                        bgColor       = WarningOrangeBg,
                        modifier      = Modifier.weight(1f)
                    ) { onDifficultyChange(Difficulty.MEDIUM) }
                    DifficultyChip(
                        label         = "Hard",
                        selected      = difficulty == Difficulty.HARD,
                        selectedColor = WrongRed,
                        bgColor       = WrongRedBg,
                        modifier      = Modifier.weight(1f)
                    ) { onDifficultyChange(Difficulty.HARD) }
                }
            }

            ConfigSection(label = "QUESTION TYPE") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuizTypeOption.entries.forEach { type ->
                        TypeChip(
                            type     = type,
                            selected = questionType == type,
                            onClick  = { onTypeChange(type) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick  = onGenerate,
                enabled  = readiness.hasEnoughContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(if (readiness.hasEnoughContent) 6.dp else 0.dp, RoundedCornerShape(14.dp)),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = StudyPurple,
                    disabledContainerColor = Color(0xFFD0D0D0)
                )
            ) {
                Text(
                    text       = if (readiness.hasEnoughContent) "Generate Quiz" else "Not Enough Content",
                    color      = Color.White,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick  = onChangeNote,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Change Note", color = StudyPurple, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReadinessCard(readiness: NoteReadinessInfo) {
    val (bg, fg, icon) = when {
        readiness.hasEnoughContent && readiness.qualityLabel == "Excellent" ->
            Triple(CorrectGreenBg, CorrectGreen, "✓")
        readiness.hasEnoughContent && readiness.qualityLabel == "Good" ->
            Triple(CorrectGreenBg, CorrectGreen, "✓")
        readiness.hasEnoughContent ->
            Triple(WarningOrangeBg, WarningOrange, "~")
        else ->
            Triple(WrongRedBg, WrongRed, "✗")
    }
    val statusText = if (readiness.hasEnoughContent) "Ready for quiz generation" else "Not enough content"

    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(28.dp).clip(CircleShape).background(fg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(statusText, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    "~${readiness.estimatedQuestions} questions possible • ${readiness.sentenceCount} sentences • ${readiness.qualityLabel}",
                    color    = fg.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ConfigSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text          = label,
            color         = TextSecondary,
            fontWeight    = FontWeight.Bold,
            fontSize      = 11.sp,
            letterSpacing = 1.5.sp
        )
        content()
    }
}

@Composable
private fun CountChip(count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(50.dp),
        color    = if (selected) StudyPurple else Color.White,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$count",
                color      = if (selected) Color.White else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize   = 15.sp
            )
        }
    }
}

@Composable
private fun TypeChip(type: QuizTypeOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) StudyPurple else Color.White,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.3f)
                        else StudyPurpleLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text       = type.label,
                color      = if (selected) Color.White else TextPrimary,
                fontSize   = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun GeneratingScreen(note: NoteModel, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                MockBackButton(onClick = onBack)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Generating Quiz", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(note.title, color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = StudyPurple, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Analyzing your note and generating questions…",
                    color     = TextSecondary,
                    fontSize  = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 40.dp),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This may take a moment for detailed notes.",
                    color    = TextHint,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuizPlayingScreen(
    state: QuizSetupUiState.Playing,
    onAnswer: (Int, String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val questions    = state.questions
    val currentIndex = state.currentIndex
    val current      = questions.getOrNull(currentIndex) ?: return
    val total        = questions.size
    val selectedOpt  = state.userAnswers[currentIndex]
    val isLastQ      = currentIndex == total - 1
    val answeredCount = state.userAnswers.size
    val optionLabels = listOf("A", "B", "C", "D")

    var showSubmitDialog by remember { mutableStateOf(false) }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Quiz?", fontWeight = FontWeight.Bold) },
            text = {
                val unanswered = total - answeredCount
                if (unanswered > 0)
                    Text("$unanswered question${if (unanswered > 1) "s" else ""} unanswered. They will count as incorrect. Submit anyway?")
                else
                    Text("All $total questions answered. Ready to see your results?")
            },
            confirmButton = {
                Button(
                    onClick = { showSubmitDialog = false; onSubmit() },
                    colors  = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Submit", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Keep Going") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    MockBackButton(onClick = onBack)
                    Text(
                        text       = "${currentIndex + 1} / $total",
                        color      = Color.White.copy(alpha = 0.85f),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    questions.forEachIndexed { idx, _ ->
                        val answered = state.userAnswers.containsKey(idx)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        idx == currentIndex -> Color.White
                                        answered            -> Color.White.copy(alpha = 0.65f)
                                        else                -> Color.White.copy(alpha = 0.22f)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = current.question,
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            current.options.forEachIndexed { i, option ->
                val label = optionLabels.getOrElse(i) { "${i + 1}" }
                MockOptionCard(
                    label      = label,
                    text       = option,
                    isSelected = selectedOpt == option,
                    onClick    = { onAnswer(currentIndex, option) }
                )
            }

            current.explanation?.let { exp ->
                if (selectedOpt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = StudyPurpleLight)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Hint", color = StudyPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = exp, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick  = onPrevious,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StudyPurple)
                ) { Text("← Previous", fontWeight = FontWeight.SemiBold) }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (isLastQ) {
                Button(
                    onClick  = { showSubmitDialog = true },
                    modifier = Modifier.weight(1f).height(52.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Submit", color = Color.White, fontWeight = FontWeight.Bold) }
            } else {
                Button(
                    onClick  = onNext,
                    modifier = Modifier.weight(1f).height(52.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Next →", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    state: QuizSetupUiState.Result,
    onRetake: () -> Unit,
    onNewQuiz: () -> Unit,
    onHistory: () -> Unit,
    onBack: () -> Unit
) {
    val attempt   = state.attempt
    val questions = state.questions
    val answers   = state.userAnswers
    val correct   = attempt.correctAnswers
    val total     = attempt.totalQuestions
    val pct       = attempt.scorePercentage
    val wrong     = total - correct

    val (grade, gradeColor, gradeMsg) = when {
        pct >= 90f -> Triple("A+", CorrectGreen, "Outstanding! Exceptional performance.")
        pct >= 80f -> Triple("A",  CorrectGreen, "Excellent! You've mastered this content.")
        pct >= 70f -> Triple("B",  StudyPurple,  "Great work! A little more practice and you'll ace it.")
        pct >= 60f -> Triple("C",  WarningOrange, "Good effort! Keep reviewing the material.")
        pct >= 50f -> Triple("D",  Color(0xFFE07B39), "You're halfway there. Focus on weak areas.")
        else       -> Triple("F",  WrongRed,     "Don't give up! Review your notes and try again.")
    }

    val difficultyColor = when (attempt.difficulty) {
        "EASY" -> CorrectGreen; "HARD" -> WrongRed; else -> WarningOrange
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MockBackButton(onClick = onBack)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier         = Modifier.size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(grade, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Quiz Complete!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text     = state.note.title,
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$correct / $total", color = StudyPurple, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Score", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${pct.toInt()}%", color = gradeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text      = gradeMsg,
                        color     = TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MockStatCard(Modifier.weight(1f), "$correct", "Correct", CorrectGreenBg, CorrectGreen)
                MockStatCard(Modifier.weight(1f), "$wrong",   "Wrong",   WrongRedBg,    WrongRed)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MockStatCard(Modifier.weight(1f), "${attempt.durationMinutes}m", "Time Used", StudyPurpleLight, StudyPurple)
                Card(
                    modifier  = Modifier.weight(1f),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = StudyPurpleLight)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text       = attempt.difficulty.lowercase().replaceFirstChar { it.uppercase() },
                            color      = difficultyColor,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("Difficulty", color = difficultyColor.copy(alpha = 0.75f), fontSize = 13.sp)
                    }
                }
            }

            Text(
                "Question Review",
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(top = 4.dp)
            )

            questions.forEachIndexed { idx, q ->
                val userAns   = answers[idx] ?: ""
                val isCorrect = userAns.equals(q.correctAnswer, ignoreCase = true)
                MockReviewCard(
                    index         = idx + 1,
                    question      = q.question,
                    userAnswer    = userAns,
                    correctAnswer = q.correctAnswer,
                    explanation   = q.explanation,
                    isCorrect     = isCorrect
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick  = onRetake,
                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(4.dp, RoundedCornerShape(14.dp)),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
            ) { Text("Retake Quiz", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = onNewQuiz,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StudyPurple)
                ) { Text("New Quiz", fontWeight = FontWeight.SemiBold) }
                OutlinedButton(
                    onClick  = onHistory,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StudyPurple)
                ) { Text("History", fontWeight = FontWeight.SemiBold) }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuizErrorScreen(message: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                MockBackButton(onClick = onBack)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Quiz Unavailable", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier            = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text       = message,
                    color      = TextSecondary,
                    fontSize   = 15.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick  = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Text("← Back to Setup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MockHistoryScreen(
    history: List<QuizAttemptModel>,
    onBack: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurpleFaint)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                MockBackButton(onClick = onBack)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Quiz History", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${history.size} attempt${if (history.size != 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier            = Modifier.padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No quiz attempts yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Generate a quiz from your notes to begin.",
                        color     = TextHint,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { attempt ->
                    MockHistoryCard(attempt = attempt, dateFmt = dateFmt)
                }
            }
        }
    }
}

@Composable
private fun MockHistoryCard(attempt: QuizAttemptModel, dateFmt: SimpleDateFormat) {
    val pct      = attempt.scorePercentage
    val barColor = when {
        pct >= 80f -> CorrectGreen
        pct >= 60f -> StudyPurple
        else       -> WrongRed
    }
    val difficultyColor = when (attempt.difficulty) {
        "EASY" -> CorrectGreen; "HARD" -> WrongRed; else -> WarningOrange
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = attempt.subject.ifBlank { attempt.title },
                        color      = TextPrimary,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(dateFmt.format(Date(attempt.completedAt)), color = TextHint, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = difficultyColor.copy(alpha = 0.12f)) {
                            Text(
                                text       = attempt.difficulty.lowercase().replaceFirstChar { it.uppercase() },
                                color      = difficultyColor,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = StudyPurpleLight) {
                            Text(
                                text       = "${attempt.totalQuestions} Qs",
                                color      = StudyPurple,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "${attempt.correctAnswers}/${attempt.totalQuestions}",
                        color      = barColor,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("${pct.toInt()}%", color = barColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress   = { pct / 100f },
                modifier   = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(10.dp)),
                color      = barColor,
                trackColor = barColor.copy(alpha = 0.14f)
            )
        }
    }
}

@Composable
private fun MockOptionCard(label: String, text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) StudyPurple else Color.White,
        animationSpec = tween(180),
        label         = "opt_bg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(180),
        label         = "opt_text"
    )
    val labelBg by animateColorAsState(
        targetValue   = if (isSelected) Color.White.copy(alpha = 0.22f) else StudyPurpleLight,
        animationSpec = tween(180),
        label         = "label_bg"
    )
    val labelTextColor by animateColorAsState(
        targetValue   = if (isSelected) Color.White else StudyPurple,
        animationSpec = tween(180),
        label         = "label_text"
    )

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(34.dp).clip(CircleShape).background(labelBg),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = labelTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text       = text,
                color      = textColor,
                fontSize   = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MockStatCard(modifier: Modifier, value: String, label: String, bg: Color, fg: Color) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = fg, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = fg.copy(alpha = 0.72f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun MockReviewCard(
    index: Int,
    question: String,
    userAnswer: String,
    correctAnswer: String,
    explanation: String?,
    isCorrect: Boolean
) {
    val bg     = if (isCorrect) CorrectGreenBg else WrongRedBg
    val accent = if (isCorrect) CorrectGreen   else WrongRed
    val icon   = if (isCorrect) "✓" else "✗"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier         = Modifier.size(24.dp).clip(CircleShape).background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = "Q$index: $question",
                    color      = TextPrimary,
                    fontSize   = 13.sp,
                    lineHeight = 19.sp,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                if (userAnswer.isNotBlank()) {
                    Text("Your answer: $userAnswer", color = WrongRed, fontSize = 12.sp)
                } else {
                    Text("Not answered", color = WrongRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Text("Correct: $correctAnswer", color = CorrectGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            if (explanation != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("💡 $explanation", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
fun DifficultyChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(50.dp),
        color    = if (selected) bgColor else Color.White,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                color      = if (selected) selectedColor else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize   = 14.sp
            )
        }
    }
}

@Composable
private fun MockBackButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint               = Color.White,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
