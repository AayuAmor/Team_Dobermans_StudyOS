package com.teamdobermans.studyos.ui.study

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier,
    viewModel: FlashcardViewModel? = null,
    onBack: () -> Unit = {}
) {
    val resolvedVm: FlashcardViewModel = viewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    val cards      by resolvedVm.flashcards.collectAsState()
    val saveResult by resolvedVm.saveResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveResult) {
        saveResult?.let {
            Toast.makeText(context, if (it == "SUCCESS") "Saved!" else it, Toast.LENGTH_SHORT).show()
            resolvedVm.clearSaveResult()
        }
    }

    FlashcardsContent(
        modifier  = modifier,
        cards     = cards,
        onBack    = onBack,
        onCreate  = { q, a, s -> resolvedVm.createFlashcard(q, a, s) },
        onUpdate  = { card -> resolvedVm.updateFlashcard(card) },
        onDelete  = { id -> resolvedVm.deleteFlashcard(id) }
    )
}

@Composable
fun FlashcardsContent(
    modifier: Modifier = Modifier,
    cards: List<FlashcardModel>,
    onBack: () -> Unit,
    onCreate: (String, String, String) -> Unit,
    onUpdate: (FlashcardModel) -> Unit,
    onDelete: (String) -> Unit
) {
    var showEditor   by rememberSaveable { mutableStateOf(false) }
    var editingCard  by remember { mutableStateOf<FlashcardModel?>(null) }
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isFlipped    by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(cards.size) {
        when {
            cards.isEmpty()              -> { currentIndex = 0; isFlipped = false }
            currentIndex >= cards.size   -> { currentIndex = cards.size - 1; isFlipped = false }
        }
    }

    if (showEditor) {
        CreateEditFlashcardScreen(
            existingCard = editingCard,
            onSave = { q, a, s ->
                if (editingCard != null) onUpdate(editingCard!!.copy(question = q, answer = a, subject = s))
                else onCreate(q, a, s)
                showEditor  = false
                editingCard = null
            },
            onDelete = { id ->
                onDelete(id)
                showEditor  = false
                editingCard = null
            },
            onBack = {
                showEditor  = false
                editingCard = null
            }
        )
    } else {
        Scaffold(
            modifier       = modifier.fillMaxSize(),
            containerColor = LightPurpleBg,
            floatingActionButton = {
                FloatingActionButton(
                    onClick        = { editingCard = null; showEditor = true },
                    modifier       = Modifier.padding(bottom = 16.dp),
                    containerColor = BrandPurple,
                    shape          = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add card", tint = Color.White)
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                FlashcardsHeader(onBackClick = onBack)

                if (cards.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No flashcards yet", color = Color.Gray, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap + to add your first card", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    val card = cards[currentIndex]

                    Column(
                        modifier              = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.spacedBy(16.dp)
                    ) {
                        FlipCard(
                            card     = card,
                            isFlipped = isFlipped,
                            onFlip   = { isFlipped = !isFlipped },
                            onEdit   = { editingCard = card; showEditor = true }
                        )

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val dotCount = minOf(cards.size, 10)
                                repeat(dotCount) { index ->
                                    Box(modifier = Modifier.size(10.dp).background(
                                        color = if (index == currentIndex) BrandPurple else BrandPurple.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ))
                                }
                            }
                            Text(
                                text  = "${currentIndex + 1} / ${cards.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                        }

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick  = { if (currentIndex > 0) { currentIndex--; isFlipped = false } },
                                enabled  = currentIndex > 0,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape    = RoundedCornerShape(22.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
                            ) { Text("← Prev") }

                            Button(
                                onClick  = { if (currentIndex < cards.size - 1) { currentIndex++; isFlipped = false } },
                                enabled  = currentIndex < cards.size - 1,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape    = RoundedCornerShape(22.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) { Text("Next →", color = Color.White) }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            RecallButton(
                                text            = stringResource(R.string.again),
                                iconRes         = R.drawable.ic_clear,
                                backgroundColor = Color(0xFFFFEBEE),
                                contentColor    = Color.Red,
                                modifier        = Modifier.weight(1f),
                                onClick         = { currentIndex = 0; isFlipped = false }
                            )
                            RecallButton(
                                text            = stringResource(R.string.got_it),
                                iconRes         = R.drawable.ic_check,
                                backgroundColor = Color(0xFFE8F5E9),
                                contentColor    = Color(0xFF4CAF50),
                                modifier        = Modifier.weight(1f),
                                onClick         = {
                                    if (currentIndex < cards.size - 1) { currentIndex++; isFlipped = false }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlipCard(
    card: FlashcardModel,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue    = if (isFlipped) 180f else 0f,
        animationSpec  = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label          = "flip"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onFlip() }
            .graphicsLayer {
                rotationY      = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            Card(
                modifier  = Modifier.fillMaxSize(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Column(
                        modifier              = Modifier.fillMaxSize(),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.Center
                    ) {
                        Text(
                            text  = stringResource(R.string.tap_to_reveal),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text        = card.question,
                            style       = MaterialTheme.typography.headlineMedium,
                            fontWeight  = FontWeight.Bold,
                            color       = BrandPurple,
                            textAlign   = TextAlign.Center
                        )
                    }
                    IconButton(
                        onClick  = onEdit,
                        modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                    ) {
                        Icon(
                            imageVector     = Icons.Default.Edit,
                            contentDescription = "Edit card",
                            tint            = BrandPurple.copy(alpha = 0.5f),
                            modifier        = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            Card(
                modifier  = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = BrandPurple.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier              = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.Center
                ) {
                    Text(text = "Answer", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = card.answer,
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CreateEditFlashcardScreen(
    existingCard: FlashcardModel? = null,
    onSave: (question: String, answer: String, subject: String) -> Unit,
    onDelete: ((cardId: String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var question         by remember { mutableStateOf(existingCard?.question ?: "") }
    var answer           by remember { mutableStateOf(existingCard?.answer ?: "") }
    val subject          = existingCard?.subject ?: "Biology"
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditing        = existingCard != null

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text("Delete Flashcard") },
            text             = { Text("Are you sure you want to delete this card?") },
            confirmButton    = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    existingCard?.let { onDelete?.invoke(it.id) }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPurpleBg)
            .imePadding()
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BrandPurple) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Button(
                    onClick        = onBack,
                    colors         = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier       = Modifier.height(32.dp)
                ) {
                    Icon(
                        painter            = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.back), color = Color.White, fontSize = 14.sp)
                }
                Text(
                    text       = if (isEditing) "Edit Card" else "New Card",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Spacer(modifier = Modifier.width(72.dp))
            }
        }

        Column(
            modifier            = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value         = question,
                onValueChange = { question = it },
                label         = { Text(stringResource(R.string.question_term)) },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    focusedLabelColor  = BrandPurple,
                    cursorColor        = BrandPurple
                )
            )

            OutlinedTextField(
                value         = answer,
                onValueChange = { answer = it },
                label         = { Text(stringResource(R.string.answer_definition)) },
                modifier      = Modifier.fillMaxWidth().weight(1f),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPurple,
                    focusedLabelColor  = BrandPurple,
                    cursorColor        = BrandPurple
                )
            )

            Button(
                onClick  = { onSave(question.trim(), answer.trim(), subject) },
                enabled  = question.isNotBlank() && answer.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = BrandPurple,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text       = if (isEditing) "Update Card" else "Save Card",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            if (isEditing && onDelete != null) {
                Button(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete Card", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun FlashcardsHeader(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = BrandPurple, shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick        = onBackClick,
                colors         = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier       = Modifier.height(32.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.back), color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = stringResource(R.string.title_activity_flashcards), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = stringResource(R.string.flashcards_subtitle), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
                Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.biology), color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24), contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
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
    onClick: () -> Unit = {}
) {
    Button(
        onClick        = onClick,
        modifier       = modifier.height(48.dp),
        colors         = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape          = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardsScreenPreview() {
    StudyOSTheme {
        FlashcardsContent(
            cards    = listOf(
                FlashcardModel(id = "1", question = "Mitosis", answer = "The process of cell division resulting in two identical daughter cells.", subject = "Biology"),
                FlashcardModel(id = "2", question = "Photosynthesis", answer = "Process by which plants convert sunlight into glucose.", subject = "Biology")
            ),
            onBack   = {},
            onCreate = { _, _, _ -> },
            onUpdate = {},
            onDelete = {}
        )
    }
}