package com.teamdobermans.studyos.ui.study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary
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
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showCreateDialog) {
        FlashcardEditDialog(
            isEditing = uiState.editingCard != null,
            front = uiState.frontInput,
            back = uiState.backInput,
            deck = uiState.deckInput,
            onFrontChange = viewModel::updateFrontInput,
            onBackChange = viewModel::updateBackInput,
            onDeckChange = viewModel::updateDeckInput,
            onSave = viewModel::saveCard,
            onDismiss = viewModel::dismissDialog
        )
    }

    if (uiState.isReviewMode) {
        ReviewModeContent(
            cards = uiState.cards,
            reviewIndex = uiState.reviewIndex,
            isFlipped = uiState.isCardFlipped,
            onFlip = viewModel::flipCard,
            onNext = viewModel::nextCard,
            onPrevious = viewModel::previousCard,
            onExit = viewModel::exitReview
        )
    } else {
        BrowseModeContent(
            decks = uiState.decks,
            selectedDeck = uiState.selectedDeck,
            cards = uiState.cards,
            isLoading = uiState.isLoading,
            onBack = onBack,
            onSelectDeck = viewModel::selectDeck,
            onStartReview = viewModel::startReview,
            onAddCard = viewModel::openCreateDialog,
            onEditCard = viewModel::openEditDialog,
            onDeleteCard = viewModel::deleteCard
        )
    }
}

@Composable
private fun BrowseModeContent(
    decks: List<String>,
    selectedDeck: String,
    cards: List<FlashcardModel>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSelectDeck: (String) -> Unit,
    onStartReview: () -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (FlashcardModel) -> Unit,
    onDeleteCard: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = BrandPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightPurpleBg)
                .padding(padding)
        ) {
            BrowseHeader(
                cardCount = cards.size,
                onBack = onBack,
                onStartReview = onStartReview
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(decks) { deck ->
                    FilterChip(
                        selected = deck == selectedDeck,
                        onClick = { onSelectDeck(deck) },
                        label = { Text(deck) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPurple)
                    }
                }
                cards.isEmpty() -> {
                    EmptyFlashcardsState(onAddCard = onAddCard)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                    ) {
                        items(cards, key = { it.id }) { card ->
                            FlashcardListItem(
                                card = card,
                                onEdit = { onEditCard(card) },
                                onDelete = { onDeleteCard(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseHeader(cardCount: Int, onBack: () -> Unit, onStartReview: () -> Unit) {
    Surface(color = BrandPurple, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }

                if (cardCount > 0) {
                    Button(
                        onClick = onStartReview,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Review all", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Flashcards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$cardCount card${if (cardCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FlashcardListItem(
    card: FlashcardModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete this card?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = BrandPurple.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = card.deckName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandPurple
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyFlashcardsState(onAddCard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No flashcards yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first card",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddCard,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Card")
        }
    }
}

@Composable
private fun ReviewModeContent(
    cards: List<FlashcardModel>,
    reviewIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExit: () -> Unit
) {
    if (cards.isEmpty()) {
        onExit()
        return
    }

    val currentCard = cards.getOrNull(reviewIndex) ?: return
    val progress = (reviewIndex + 1f) / cards.size
    val pixelDensity = LocalDensity.current.density

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurpleBg)
    ) {
        Surface(color = BrandPurple, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${reviewIndex + 1} of ${cards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "Exit review", tint = Color.White)
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = BrandPurple,
            trackColor = BrandPurple.copy(alpha = 0.2f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable(onClick = onFlip)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * pixelDensity
                            alpha = if (rotation < 90f) 1f else 0f
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QUESTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandPurple.copy(alpha = 0.5f),
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = currentCard.front,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = rotation - 180f
                            cameraDistance = 12f * pixelDensity
                            alpha = if (rotation >= 90f) 1f else 0f
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPurple),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ANSWER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = currentCard.back,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isFlipped) "Tap card to show question" else "Tap card to reveal answer",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = reviewIndex > 0,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = onNext,
                    enabled = reviewIndex < cards.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Next")
                }
            }

            if (reviewIndex == cards.size - 1) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "All cards reviewed!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandPurple,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onExit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
                ) {
                    Text("Back to cards")
                }
            }
        }
    }
}

@Composable
private fun FlashcardEditDialog(
    isEditing: Boolean,
    front: String,
    back: String,
    deck: String,
    onFrontChange: (String) -> Unit,
    onBackChange: (String) -> Unit,
    onDeckChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Card" else "Add Card",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = deck,
                    onValueChange = onDeckChange,
                    label = { Text("Deck name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        focusedLabelColor = BrandPurple
                    )
                )
                OutlinedTextField(
                    value = front,
                    onValueChange = onFrontChange,
                    label = { Text("Question (front)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        focusedLabelColor = BrandPurple
                    )
                )
                OutlinedTextField(
                    value = back,
                    onValueChange = onBackChange,
                    label = { Text("Answer (back)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        focusedLabelColor = BrandPurple
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = front.isNotBlank() && back.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}