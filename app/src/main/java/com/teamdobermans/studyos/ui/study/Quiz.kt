package com.teamdobermans.studyos.ui.study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizQuestionModel
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.QuizFlowState
import com.teamdobermans.studyos.viewModel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QuizPurple      = Color(0xFF5B4DDB)
private val QuizPurpleDark  = Color(0xFF3D30A0)
private val QuizPurpleLight = Color(0xFFEAE5FF)
private val QuizGradient    = listOf(Color(0xFF5B4DDB), Color(0xFF6A5AF9))
private val CorrectGreen    = Color(0xFF1B7A3E)
private val CorrectGreenBg  = Color(0xFFE8F5E9)
private val WrongRed        = Color(0xFFE53935)
private val WrongRedBg      = Color(0xFFFFF0F0)

class QuizScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteId    = intent.getStringExtra("noteId")
        val noteTitle = intent.getStringExtra("noteTitle") ?: ""
        val noteBody  = intent.getStringExtra("noteBody") ?: ""

        setContent {
            StudyOSTheme {
                val vm: QuizViewModel = viewModel()

                LaunchedEffect(Unit) {
                    if (noteId != null && noteBody.isNotBlank()) {
                        vm.generateQuiz(
                            NoteModel(id = noteId, title = noteTitle, body = noteBody)
                        )
                    }
                }

                QuizRoot(vm = vm, onFinish = { finish() })
            }
        }
    }
}

@Composable
fun QuizRoot(vm: QuizViewModel, onFinish: () -> Unit) {
    val quizState    by vm.quizState.collectAsState()
    val notes        by vm.notes.collectAsState()
    val notesLoading by vm.notesLoading.collectAsState()
    val attempts     by vm.attempts.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        QuizHistoryScreen(
            attempts = attempts,
            onBack   = { showHistory = false }
        )
        return
    }

    when (val state = quizState) {
        is QuizFlowState.Idle, is QuizFlowState.Error -> {
            NotePickerScreen(
                notes        = notes,
                isLoading    = notesLoading,
                errorMessage = (quizState as? QuizFlowState.Error)?.message,
                onSelectNote = { vm.generateQuiz(it) },
                onShowHistory = { showHistory = true },
                onBack        = onFinish
            )
        }

        QuizFlowState.GeneratingQuiz -> {
            GeneratingScreen()
        }

        is QuizFlowState.Playing -> {
            QuizPlayingScreen(
                state      = state,
                onAnswer   = { idx, ans -> vm.selectAnswer(idx, ans) },
                onNext     = { vm.nextQuestion() },
                onPrevious = { vm.previousQuestion() },
                onSubmit   = { vm.submitQuiz() },
                onBack     = { vm.reset() }
            )
        }

        is QuizFlowState.Result -> {
            QuizResultScreen(
                state     = state,
                onRetake  = { vm.retakeQuiz() },
                onNewQuiz = { vm.reset() },
                onHistory = { showHistory = true },
                onFinish  = onFinish
            )
        }
    }
}

@Composable
private fun NotePickerScreen(
    notes: List<NoteModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectNote: (NoteModel) -> Unit,
    onShowHistory: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
    ) {
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
                    QuizBackButton(onClick = onBack)
                    IconButton(onClick = onShowHistory) {
                        Icon(
                            imageVector        = Icons.Default.History,
                            contentDescription = "History",
                            tint               = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "Generate Quiz",
                    color      = Color.White,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Pick a note to quiz yourself on",
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        if (errorMessage != null) {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors    = CardDefaults.cardColors(containerColor = WrongRedBg),
                shape     = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text      = errorMessage,
                    color     = WrongRed,
                    fontSize  = 14.sp,
                    modifier  = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = QuizPurple)
                }
            }

            notes.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = "No notes yet",
                            color      = TextSecondary,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = "Create some notes first, then come back to generate a quiz.",
                            color     = TextHint,
                            fontSize  = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NotePickerCard(note = note, onClick = { onSelectNote(note) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotePickerCard(note: NoteModel, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = note.title,
                    color      = TextPrimary,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = note.body.take(80).replace("\n", " "),
                    color    = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(QuizPurpleLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text       = note.folder,
                        color      = QuizPurple,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(QuizPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▶", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun GeneratingScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                modifier  = Modifier
                    .size(96.dp)
                    .shadow(8.dp, CircleShape),
                shape     = CircleShape,
                colors    = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(QuizGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        strokeWidth = 3.dp,
                        modifier    = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text       = "Generating your quiz…",
                color      = TextPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text     = "Analysing note content and creating questions",
                color    = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun QuizPlayingScreen(
    state: QuizFlowState.Playing,
    onAnswer: (Int, String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val questions    = state.quiz.questions
    val currentIndex = state.currentIndex
    val current      = questions.getOrNull(currentIndex) ?: return
    val total        = questions.size
    val selectedOpt  = state.userAnswers[currentIndex]
    val isLastQ      = currentIndex == total - 1
    val answeredCount = state.userAnswers.size

    var showSubmitDialog by remember { mutableStateOf(false) }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Quiz?", fontWeight = FontWeight.Bold) },
            text  = {
                val unanswered = total - answeredCount
                if (unanswered > 0) {
                    Text("You have $unanswered unanswered question${if (unanswered > 1) "s" else ""}. Unanswered questions will be counted as wrong. Submit anyway?")
                } else {
                    Text("You have answered all $total questions. Ready to see your score?")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSubmitDialog = false; onSubmit() },
                    colors  = ButtonDefaults.buttonColors(containerColor = QuizPurple)
                ) { Text("Submit", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Review") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
    ) {
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
                    QuizBackButton(onClick = onBack)
                    Text(
                        text       = "Question ${currentIndex + 1} of $total",
                        color      = Color.White.copy(alpha = 0.85f),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                                        answered            -> Color.White.copy(alpha = 0.7f)
                                        else                -> Color.White.copy(alpha = 0.25f)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = current.question,
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
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
            val optionLabels = listOf("A", "B", "C", "D")
            current.options.forEachIndexed { i, text ->
                QuizOptionCard(
                    label      = optionLabels.getOrElse(i) { "${i + 1}" },
                    text       = text,
                    isSelected = selectedOpt == text,
                    onClick    = { onAnswer(currentIndex, text) }
                )
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
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape   = RoundedCornerShape(14.dp),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = QuizPurple)
                ) {
                    Text("← Previous", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (isLastQ) {
                Button(
                    onClick  = { showSubmitDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QuizPurple)
                ) {
                    Text("Submit Quiz", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick  = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QuizPurple)
                ) {
                    Text("Next →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuizOptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) QuizPurple else Color.White,
        animationSpec = tween(200),
        label = "option_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(200),
        label = "option_text"
    )
    val labelBg by animateColorAsState(
        targetValue = if (isSelected) Color.White.copy(alpha = 0.25f) else QuizPurpleLight,
        animationSpec = tween(200),
        label = "label_bg"
    )
    val labelText by animateColorAsState(
        targetValue = if (isSelected) Color.White else QuizPurple,
        animationSpec = tween(200),
        label = "label_text"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(labelBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    color      = labelText,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text       = text,
                color      = textColor,
                fontSize   = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuizResultScreen(
    state: QuizFlowState.Result,
    onRetake: () -> Unit,
    onNewQuiz: () -> Unit,
    onHistory: () -> Unit,
    onFinish: () -> Unit
) {
    val attempt   = state.attempt
    val questions = state.questions
    val answers   = state.userAnswers
    val correct   = attempt.correctAnswers
    val total     = attempt.totalQuestions
    val pct       = attempt.scorePercentage
    val wrong     = total - correct

    val (grade, gradeColor, gradeMsg) = when {
        pct >= 90f -> Triple("A+", CorrectGreen, "Excellent! Outstanding performance!")
        pct >= 80f -> Triple("A",  CorrectGreen, "Great job! You've mastered this topic.")
        pct >= 70f -> Triple("B",  QuizPurple,   "Good work! A bit more practice will help.")
        pct >= 60f -> Triple("C",  Color(0xFFC97B00), "Keep going! You're getting there.")
        else       -> Triple("D",  WrongRed,     "Keep studying! Review your notes and try again.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text       = "Quiz Completed!",
                    color      = Color.White,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = attempt.subject,
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier         = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = grade,
                            color      = Color.White,
                            fontSize   = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
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
                    modifier              = Modifier.padding(24.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "$correct / $total",
                        color      = QuizPurple,
                        fontSize   = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text     = "Score",
                        color    = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text       = "${pct.toInt()}%",
                        color      = gradeColor,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text      = gradeMsg,
                        color     = TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    value    = "$correct",
                    label    = "Correct",
                    bg       = CorrectGreenBg,
                    fg       = CorrectGreen
                )
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    value    = "$wrong",
                    label    = "Wrong",
                    bg       = WrongRedBg,
                    fg       = WrongRed
                )
            }

            Text(
                text       = "Question Review",
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(top = 4.dp)
            )

            questions.forEachIndexed { idx, q ->
                val userAns = answers[idx] ?: ""
                val correct = userAns.equals(q.correctAnswer, ignoreCase = true)
                ReviewCard(
                    index       = idx + 1,
                    question    = q.question,
                    userAnswer  = userAns,
                    correctAnswer = q.correctAnswer,
                    isCorrect   = correct
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick  = onRetake,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuizPurple)
            ) {
                Text("Retake Quiz", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onNewQuiz,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = QuizPurple)
                ) {
                    Text("New Quiz", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick  = onHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = QuizPurple)
                ) {
                    Text("History", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResultStatCard(
    modifier: Modifier,
    value: String,
    label: String,
    bg: Color,
    fg: Color
) {
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
            Text(value, color = fg, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = fg.copy(alpha = 0.75f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReviewCard(
    index: Int,
    question: String,
    userAnswer: String,
    correctAnswer: String,
    isCorrect: Boolean
) {
    val bg     = if (isCorrect) CorrectGreenBg else WrongRedBg
    val accent = if (isCorrect) CorrectGreen   else WrongRed
    val icon   = if (isCorrect) "✓" else "✗"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier         = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = "Q$index: ${question.removePrefix("Fill in the blank:\n")}",
                    color      = TextPrimary,
                    fontSize   = 13.sp,
                    lineHeight = 19.sp,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (!isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                if (userAnswer.isNotBlank()) {
                    Text(
                        text     = "Your answer: $userAnswer",
                        color    = WrongRed,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text     = "Not answered",
                        color    = WrongRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text       = "Correct: $correctAnswer",
                    color      = CorrectGreen,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun QuizHistoryScreen(
    attempts: List<QuizAttemptModel>,
    onBack: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(QuizGradient))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Column {
                QuizBackButton(onClick = onBack)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "Quiz History",
                    color      = Color.White,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "${attempts.size} attempt${if (attempts.size != 1) "s" else ""}",
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        if (attempts.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "No quiz attempts yet",
                        color      = TextSecondary,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = "Complete a quiz to see your history here.",
                        color     = TextHint,
                        fontSize  = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attempts, key = { it.id }) { attempt ->
                    HistoryAttemptCard(attempt = attempt, dateFmt = dateFmt)
                }
            }
        }
    }
}

@Composable
private fun HistoryAttemptCard(
    attempt: QuizAttemptModel,
    dateFmt: SimpleDateFormat
) {
    val pct      = attempt.scorePercentage
    val barColor = when {
        pct >= 80f -> CorrectGreen
        pct >= 60f -> QuizPurple
        else       -> WrongRed
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
                    Text(
                        text     = dateFmt.format(Date(attempt.completedAt)),
                        color    = TextHint,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text       = "${attempt.correctAnswers}/${attempt.totalQuestions}",
                    color      = barColor,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress         = { pct / 100f },
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color            = barColor,
                trackColor       = barColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text     = "${pct.toInt()}%",
                color    = barColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuizBackButton(onClick: () -> Unit) {
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
        Text(text = "Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
