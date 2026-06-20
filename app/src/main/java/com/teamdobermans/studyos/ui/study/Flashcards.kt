package com.teamdobermans.studyos.ui.study

import com.teamdobermans.studyos.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.viewModel.FlashcardViewModel

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
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: FlashcardViewModel = viewModel()
) {
    val currentCard by viewModel.currentCard.collectAsState()
    val dueCards by viewModel.dueCards.collectAsState()

    // Load due cards when screen opens
    LaunchedEffect(Unit) { viewModel.loadDueCards() }

    FlashcardsScreenContent(
        currentCard = currentCard,
        dueCards = dueCards,
        modifier = modifier,
        onBack = onBack,
        onAddCard = { question, answer ->
            viewModel.addCard(question, answer)
        },
        onSubmitReview = { card, quality ->
            viewModel.submitReview(card, quality)
        }
    )
}

@Composable
fun FlashcardsScreenContent(
    currentCard: FlashcardModel?,
    dueCards: List<FlashcardModel>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddCard: (String, String) -> Unit = { _, _ -> },
    onSubmitReview: (FlashcardModel, Int) -> Unit = { _, _ -> }
) {
    var showAnswer by remember { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }

    // Reset card flip whenever the card changes
    LaunchedEffect(currentCard) { showAnswer = false }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightPurpleBg)
    ) {
        FlashcardsHeader(onBackClick = onBack)

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Flashcard ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { if (currentCard != null) showAnswer = !showAnswer },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentCard == null) {
                        // No cards due state
                        Text(
                            text = "🎉 All caught up!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No cards due for review.\nAdd new cards below.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = if (showAnswer) "Answer" else "Question — tap to reveal",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showAnswer) currentCard.answer else currentCard.question,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Progress dots + counter ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotCount = dueCards.size.coerceIn(1, 7)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(dotCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (index == 0) BrandPurple else BrandPurple.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
                Text(
                    text = if (dueCards.isEmpty()) "No cards due"
                    else "${dueCards.size} card${if (dueCards.size > 1) "s" else ""} remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            // ── Again / Got it buttons (only shown after revealing) ────
            if (currentCard != null && showAnswer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecallButton(
                        text = stringResource(R.string.again),
                        iconRes = R.drawable.ic_clear,
                        backgroundColor = Color(0xFFFFEBEE),
                        contentColor = Color.Red,
                        modifier = Modifier.weight(1f),
                        onClick = { onSubmitReview(currentCard, 1) }
                    )
                    RecallButton(
                        text = stringResource(R.string.got_it),
                        iconRes = R.drawable.ic_check,
                        backgroundColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                        onClick = { onSubmitReview(currentCard, 5) }
                    )
                }
            } else if (currentCard != null) {
                // Placeholder so layout doesn't jump
                Spacer(modifier = Modifier.height(48.dp))
            }

            // ── Add Card form ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_card),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                    TextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = {
                            Text(stringResource(R.string.question_term), color = Color.White.copy(alpha = 0.7f))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BrandPurple,
                            unfocusedContainerColor = BrandPurple,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    TextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = {
                            Text(stringResource(R.string.answer_definition), color = Color.White.copy(alpha = 0.7f))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BrandPurple,
                            unfocusedContainerColor = BrandPurple,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (questionText.isNotBlank() && answerText.isNotBlank()) {
                                onAddCard(
                                    questionText.trim(),
                                    answerText.trim()
                                )
                                questionText = ""
                                answerText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (questionText.isNotBlank() && answerText.isNotBlank())
                                BrandPurple else Color(0xFFF0F7FF)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.add_card_button),
                            color = if (questionText.isNotBlank() && answerText.isNotBlank())
                                Color.White else Color(0xFFAAB8C2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardsHeader(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BrandPurple,
        shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.back), color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.title_activity_flashcards),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.flashcards_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.biology),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecallButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}          // ← now wired up
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardsScreenPreview() {
    val mockCard = FlashcardModel(
        question = "What is Photosynthesis?",
        answer = "The process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water."
    )
    StudyOSTheme {
        FlashcardsScreenContent(
            currentCard = mockCard,
            dueCards = listOf(mockCard)
        )
    }
}