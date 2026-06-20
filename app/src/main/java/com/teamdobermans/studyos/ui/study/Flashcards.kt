package com.teamdobermans.studyos.ui.study

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
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
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
    onBack: () -> Unit = {},
    viewModel: FlashcardViewModel = viewModel()
) {
    val currentCard by viewModel.currentCard.collectAsState()
    val dueCards by viewModel.dueCards.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDueCards()
    }

    FlashcardsScreenContent(
        currentCard = currentCard,
        dueCardsCount = dueCards.size,
        onBack = onBack,
        onReview = { quality ->
            currentCard?.let { viewModel.submitReview(it, quality) }
        },
        onAddCard = { question, answer ->
            viewModel.addCard(question, answer)
        }
    )
}

@Composable
fun FlashcardsScreenContent(
    currentCard: FlashcardModel?,
    dueCardsCount: Int,
    onBack: () -> Unit,
    onReview: (Int) -> Unit,
    onAddCard: (String, String) -> Unit
) {
    var showAnswer by remember(currentCard) { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showAnswer = !showAnswer },
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
                        Text(
                            text = stringResource(R.string.no_cards_due),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandPurple
                        )
                    } else {
                        Text(
                            text = if (showAnswer) stringResource(R.string.answer) else stringResource(R.string.question),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(minOf(dueCardsCount, 7)) { index ->
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
                    text = stringResource(R.string.swipe_or_tap),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

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
                    onClick = { onReview(1) }
                )
                RecallButton(
                    text = stringResource(R.string.got_it),
                    iconRes = R.drawable.ic_check,
                    backgroundColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { onReview(5) }
                )
            }

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
                        placeholder = { Text(stringResource(R.string.question_term), color = Color.White.copy(alpha = 0.7f)) },
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
                        placeholder = { Text(stringResource(R.string.answer_definition), color = Color.White.copy(alpha = 0.7f)) },
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
                                onAddCard(questionText, answerText)
                                questionText = ""
                                answerText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F7FF))
                    ) {
                        Text(
                            text = stringResource(R.string.add_card_button),
                            color = Color(0xFF4A90E2),
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
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
    StudyOSTheme {
        FlashcardsScreenContent(
            currentCard = FlashcardModel(
                question = "What is Mitosis?",
                answer = "A type of cell division that results in two daughter cells each having the same number and kind of chromosomes as the parent nucleus."
            ),
            dueCardsCount = 5,
            onBack = {},
            onReview = {},
            onAddCard = { _, _ -> }
        )
    }
}
