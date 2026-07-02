package com.teamdobermans.studyos.ui.focus

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.model.BrainGameMode
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.components.StudyOSSecondaryButton
import com.teamdobermans.studyos.ui.navigation.AppRoutes
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
import com.teamdobermans.studyos.viewModel.BrainGameUiState
import com.teamdobermans.studyos.viewModel.BrainGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun BrainGameScreen(
    mode: String = AppRoutes.BrainGame.MODE_HUB,
    sessionId: String? = null,
    onBack: () -> Unit = {}
) {
    val viewModel: BrainGameViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val selectedMode = BrainGameMode.fromRoute(mode)
    val context = LocalContext.current

    LaunchedEffect(selectedMode, sessionId) {
        if (selectedMode == null) {
            viewModel.loadBestScores()
        } else {
            viewModel.startGame(selectedMode, relatedSessionId = sessionId, playedAfterSession = !sessionId.isNullOrBlank())
            viewModel.loadBestScores(selectedMode)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleFaint),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { BrainGameHeader(mode = selectedMode, onBack = onBack) }
        when (selectedMode) {
            BrainGameMode.MEMORY_MATCH -> item {
                MemoryMatchModeCard(
                    uiState = uiState,
                    onStart = {
                        viewModel.startGame(
                            BrainGameMode.MEMORY_MATCH,
                            relatedSessionId = sessionId,
                            playedAfterSession = !sessionId.isNullOrBlank()
                        )
                    },
                    onComplete = { viewModel.onMemoryMatchCompleted(it) },
                    onMove = { viewModel.onMemoryMatchMove(it) },
                    onBack = onBack
                )
            }
            BrainGameMode.MATH_SPRINT -> item {
                MathSprintModeCard(
                    uiState = uiState,
                    onStart = {
                        viewModel.startGame(
                            BrainGameMode.MATH_SPRINT,
                            relatedSessionId = sessionId,
                            playedAfterSession = !sessionId.isNullOrBlank()
                        )
                    },
                    onAnswer = { correct -> viewModel.onMathAnswer(correct) },
                    onBack = onBack
                )
            }
            else -> item {
                BrainGamesHub(
                    uiState = uiState,
                    onSelectMode = { selected ->
                        if (uiState.isPlaying) {
                            Toast.makeText(context, "Finish or exit the current game first", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.startGame(selected)
                            viewModel.loadBestScores(selected)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BrainGameHeader(mode: BrainGameMode?, onBack: () -> Unit) {
    val title = when (mode) {
        BrainGameMode.MEMORY_MATCH -> "Memory Match"
        BrainGameMode.MATH_SPRINT -> "Math Sprint"
        else -> "Brain Games"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp)
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "A quick brain break between study sessions.",
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BrainGamesHub(uiState: BrainGameUiState, onSelectMode: (BrainGameMode) -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recent best score", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    val bestLabel = if (uiState.bestScore > 0) "Your best break-game score is ${uiState.bestScore}" else "Play a round to save your first score"
                    Text(bestLabel, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        BrainGameInfoCard(
            icon = Icons.Rounded.GridView,
            title = "Memory Match",
            subtitle = "Match pairs and train recall during study breaks.",
            accent = Color(0xFFE04F7B),
            onClick = {
                if (uiState.isPlaying) {
                    Toast.makeText(context, "Finish or exit the current game first", Toast.LENGTH_SHORT).show()
                } else {
                    onSelectMode(BrainGameMode.MEMORY_MATCH)
                }
            }
        )
        BrainGameInfoCard(
            icon = Icons.Rounded.Calculate,
            title = "Math Sprint",
            subtitle = "Rapid arithmetic practice for a quick cognitive reset.",
            accent = Color(0xFF4B5BE0),
            onClick = {
                if (uiState.isPlaying) {
                    Toast.makeText(context, "Finish or exit the current game first", Toast.LENGTH_SHORT).show()
                } else {
                    onSelectMode(BrainGameMode.MATH_SPRINT)
                }
            }
        )
    }
}

@Composable
private fun MemoryMatchModeCard(
    uiState: BrainGameUiState,
    onStart: () -> Unit,
    onComplete: (Int) -> Unit,
    onMove: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val symbols = listOf("A", "B", "C", "D", "E", "F")
    val cards = remember(uiState.isPlaying, uiState.isCompleted, uiState.mode) {
        (symbols + symbols).shuffled(Random(System.currentTimeMillis()))
    }
    var selectedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matchedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isPlaying, uiState.isCompleted) {
        selectedIndices = emptyList()
        matchedIndices = emptySet()
        elapsedSeconds = 0
        if (uiState.isPlaying) {
            while (true) {
                delay(1000L)
                if (!uiState.isPlaying) break
                elapsedSeconds += 1
            }
        }
    }

    ModeCard(icon = Icons.Rounded.GridView, title = "Memory Match", accent = Color(0xFFE04F7B)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatLabel("Score", uiState.score.toString())
            StatLabel("Best", if (uiState.bestScore > 0) uiState.bestScore.toString() else "—")
            StatLabel("Moves", uiState.moves.toString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!uiState.isPlaying && !uiState.isCompleted) {
            StudyOSPrimaryButton(text = "Start Game", onClick = onStart, modifier = Modifier.fillMaxWidth())
        } else if (uiState.isCompleted) {
            ResultCard(
                title = "Great memory work!",
                detail = "You cleared the board in $elapsedSeconds seconds with ${uiState.moves} moves.",
                score = uiState.score,
                secondaryDetail = "Best score: ${if (uiState.bestScore > 0) uiState.bestScore else "—"}",
                primaryAction = { onStart() },
                primaryLabel = "Play Again",
                secondaryAction = onBack,
                secondaryLabel = "Back to Focus"
            )
        } else {
            Text("Find every pair before time runs out.", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cards.chunked(4).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { columnIndex, label ->
                            val index = rowIndex * 4 + columnIndex
                            val isVisible = matchedIndices.contains(index) || selectedIndices.contains(index)
                            Card(
                                modifier = Modifier.size(72.dp).clickable {
                                    if (!uiState.isPlaying || uiState.isCompleted || matchedIndices.contains(index) || selectedIndices.contains(index)) return@clickable
                                    val nextSelection = selectedIndices + index
                                    selectedIndices = nextSelection
                                    onMove(true)
                                    if (nextSelection.size == 2) {
                                        val first = nextSelection[0]
                                        val second = nextSelection[1]
                                        val isMatch = cards[first] == cards[second]
                                        if (isMatch) {
                                            matchedIndices = matchedIndices + first + second
                                            if (matchedIndices.size == cards.size) {
                                                onComplete(elapsedSeconds)
                                            }
                                            selectedIndices = emptyList()
                                        } else {
                                            scope.launch {
                                                delay(700)
                                                selectedIndices = emptyList()
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isVisible) StudyPurpleLight else Color.White)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(if (isVisible) label else "?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StudyPurple)
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
private fun MathSprintModeCard(
    uiState: BrainGameUiState,
    onStart: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val question = remember(uiState.correctAnswers, uiState.wrongAnswers, uiState.isPlaying, uiState.mode) {
        generateMathQuestion()
    }
    val choices = remember(question) { generateChoices(question.second) }

    ModeCard(icon = Icons.Rounded.Calculate, title = "Math Sprint", accent = Color(0xFF4B5BE0)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatLabel("Score", uiState.score.toString())
            StatLabel("Best", if (uiState.bestScore > 0) uiState.bestScore.toString() else "—")
            StatLabel("Time", uiState.timeRemainingSeconds.toString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!uiState.isPlaying && !uiState.isCompleted) {
            StudyOSPrimaryButton(text = "Start Game", onClick = onStart, modifier = Modifier.fillMaxWidth())
        } else if (uiState.isCompleted) {
            ResultCard(
                title = "Math sprint complete!",
                detail = "You answered ${uiState.correctAnswers} correctly and ${uiState.wrongAnswers} incorrectly.",
                score = uiState.score,
                secondaryDetail = "Accuracy: ${formatAccuracy(uiState.correctAnswers, uiState.wrongAnswers)}",
                primaryAction = { onStart() },
                primaryLabel = "Play Again",
                secondaryAction = onBack,
                secondaryLabel = "Back to Focus"
            )
        } else {
            Text(question.first, color = StudyPurpleDeep, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Choose the correct answer", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                choices.forEach { option ->
                    StudyOSSecondaryButton(
                        text = option.toString(),
                        onClick = { onAnswer(option == question.second) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    detail: String,
    score: Int,
    secondaryDetail: String,
    primaryAction: () -> Unit,
    primaryLabel: String,
    secondaryAction: () -> Unit,
    secondaryLabel: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(detail, color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Score $score", color = StudyPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(secondaryDetail, color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(14.dp))
            StudyOSPrimaryButton(text = primaryLabel, onClick = primaryAction, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            StudyOSSecondaryButton(text = secondaryLabel, onClick = secondaryAction, modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun generateMathQuestion(): Pair<String, Int> {
    val left = Random.nextInt(1, 13)
    val right = Random.nextInt(1, 13)
    val op = listOf("+", "-", "×").random()
    return when (op) {
        "+" -> "$left + $right" to left + right
        "-" -> "$left - $right" to left - right
        else -> "$left × $right" to left * right
    }
}

private fun generateChoices(answer: Int): List<Int> {
    val base = mutableSetOf(answer)
    while (base.size < 4) {
        base.add((answer + Random.nextInt(-4, 5)).coerceIn(1, 144))
    }
    return base.shuffled()
}

private fun formatAccuracy(correct: Int, wrong: Int): String {
    val total = correct + wrong
    return if (total > 0) "${(correct * 100f / total).toInt()}%" else "0%"
}

@Composable
private fun ModeCard(icon: ImageVector, title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun BrainGameInfoCard(icon: ImageVector, title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = StudyPurpleLight)
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = StudyPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBrainGameScreen() {
    StudyOSTheme { BrainGameScreen() }
}
