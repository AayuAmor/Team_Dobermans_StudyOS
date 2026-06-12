package com.teamdobermans.studyos.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.model.VisionGoalModel
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.VisionBoardViewModel

class VisionBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "mock_user_id"
        
        if (currentUser == null) {
            android.util.Log.w("VisionBoard", "No user found, using mock_user_id for testing")
            Toast.makeText(this, "Testing with mock user", Toast.LENGTH_SHORT).show()
        } else {
            android.util.Log.d("VisionBoard", "User authenticated: $uid")
        }

        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                VisionBoardBody(onBack = { finish() })
            }
        }
    }
}

@Composable
fun VisionBoardBody(
    viewModel: VisionBoardViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val subjects     by viewModel.subjects.collectAsState()
    val goalText        by viewModel.goalText.collectAsState()
    val selectedEmoji   by viewModel.selectedEmoji.collectAsState()
    val targetValue     by viewModel.targetValue.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val pinnedGoals     by viewModel.pinnedGoals.collectAsState()
    val editingGoal     by viewModel.editingGoal.collectAsState()

    val context = LocalContext.current

    VisionBoardContent(
        goalText            = goalText,
        selectedEmoji       = selectedEmoji,
        targetValue         = targetValue,
        selectedSubject     = selectedSubject,
        subjects            = subjects,
        pinnedGoals         = pinnedGoals,
        editingGoal         = editingGoal,
        onGoalTextChange    = viewModel::setGoalText,
        onEmojiSelect       = viewModel::setEmoji,
        onTargetValueChange = viewModel::setTargetValue,
        onSubjectSelect     = viewModel::selectSubject,
        onAddSubject        = viewModel::addSubject,
        onPinGoal           = {
            viewModel.pinGoal { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        },
        onRemoveGoal        = viewModel::removeGoal,
        onStartEditing      = viewModel::startEditing,
        onEditGoal          = viewModel::editGoal,
        onStopEditing       = viewModel::stopEditing,
        onBack              = onBack
    )
}

@Composable
fun VisionBoardContent(
    goalText: String,
    selectedEmoji: String,
    targetValue: String,
    selectedSubject: String,
    subjects: List<String>,
    pinnedGoals: List<VisionGoalModel>,
    editingGoal: VisionGoalModel?,
    onGoalTextChange: (String) -> Unit,
    onEmojiSelect: (String) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onSubjectSelect: (String) -> Unit,
    onAddSubject: (String) -> Unit,
    onPinGoal: () -> Unit,
    onRemoveGoal: (VisionGoalModel) -> Unit,
    onStartEditing: (VisionGoalModel) -> Unit,
    onEditGoal: (VisionGoalModel) -> Unit,
    onStopEditing: () -> Unit,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val emojiOptions = listOf("🏅", "⭐", "💪", "🚀", "💵")
    val canPin       = goalText.trim().isNotEmpty()

    var subjectDropdown by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    if (showAddSubjectDialog) {
        var newSubject by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add Subject", fontWeight = FontWeight.Bold, color = StudyPurple) },
            text = {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    placeholder = { Text("Enter subject name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudyPurple,
                        cursorColor = StudyPurple
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubject.isNotBlank()) {
                            onAddSubject(newSubject)
                            onSubjectSelect(newSubject)
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Add", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel", color = StudyPurple)
                }
            }
        )
    }


    editingGoal?.let { goal ->
        var editText   by remember(goal.id) { mutableStateOf(goal.text) }
        var editTarget by remember(goal.id) { mutableStateOf(goal.targetValue) }
        var editEmoji  by remember(goal.id) { mutableStateOf(goal.emoji) }

        AlertDialog(
            onDismissRequest = onStopEditing,
            title = {
                Text("Edit Goal", fontWeight = FontWeight.Bold, color = StudyPurple)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value         = editText,
                        onValueChange = { editText = it },
                        label         = { Text("Goal") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudyPurple,
                            cursorColor        = StudyPurple
                        )
                    )
                    OutlinedTextField(
                        value         = editTarget,
                        onValueChange = { editTarget = it },
                        label         = { Text("Target value") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudyPurple,
                            cursorColor        = StudyPurple
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojiOptions.forEach { emoji ->
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (emoji == editEmoji)
                                    StudyPurple.copy(alpha = 0.15f) else StudyPurpleFaint,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { editEmoji = emoji }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier         = Modifier.fillMaxSize()
                                ) {
                                    Text(emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditGoal(
                            goal.copy(
                                text        = editText,
                                targetValue = editTarget,
                                emoji       = editEmoji
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = onStopEditing) {
                    Text("Cancel", color = StudyPurple)
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurple)
            .imePadding()
    ) {

        // Top bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter           = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint              = Color.White,
                        modifier          = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Vision Board",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                "Goals - Images - Subject targets",
                color    = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp
            )
        }

        // Scrollable body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(StudyPurpleFaint)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Add goal",
                        color      = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value         = goalText,
                        onValueChange = onGoalTextChange,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true,
                        placeholder   = { Text("Your goal or dream...", color = TextHint) },
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = StudyPurple,
                            unfocusedBorderColor    = StudyPurpleLight,
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor             = StudyPurple,
                            focusedTextColor        = TextPrimary,
                            unfocusedTextColor      = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojiOptions.forEach { emoji ->
                            Surface(
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (emoji == selectedEmoji)
                                    StudyPurple.copy(alpha = 0.15f) else StudyPurpleFaint,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onEmojiSelect(emoji) }
                            ) {
                                Box(
                                    modifier         = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject picker
                    Text(
                        "Subject",
                        color      = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = StudyPurpleLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { subjectDropdown = true }
                        ) {
                            Row(
                                modifier                = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment       = Alignment.CenterVertically,
                                horizontalArrangement   = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    selectedSubject,
                                    color      = StudyPurple,
                                    fontWeight = FontWeight.Medium,
                                    fontSize   = 14.sp
                                )
                                Icon(
                                    painter            = painterResource(R.drawable.baseline_more_horiz_24),
                                    contentDescription = null,
                                    tint               = StudyPurple,
                                    modifier           = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded          = subjectDropdown,
                            onDismissRequest  = { subjectDropdown = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            subject,
                                            fontWeight = if (subject == selectedSubject)
                                                FontWeight.Bold else FontWeight.Normal,
                                            color = if (subject == selectedSubject)
                                                StudyPurple else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        onSubjectSelect(subject)
                                        subjectDropdown = false
                                    }
                                )
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            DropdownMenuItem(
                                text    = { Text("Add subject...", color = StudyPurple, fontWeight = FontWeight.SemiBold) },
                                onClick = { 
                                    subjectDropdown = false
                                    showAddSubjectDialog = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target value
                    Text(
                        "Target value",
                        color      = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = targetValue,
                        onValueChange = {
                            onTargetValueChange(
                                it.filter { c -> c.isDigit() || c == '%' }.take(10)
                            )
                        },
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = RoundedCornerShape(10.dp),
                        singleLine      = true,
                        placeholder     = { Text("e.g. 100 pages, 90%", color = TextHint) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = StudyPurple,
                            unfocusedBorderColor    = StudyPurple.copy(alpha = 0.4f),
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor             = StudyPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick  = onPinGoal,
                        enabled  = canPin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(25.dp)),
                        shape  = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                    ) {
                        Text("Pin to Board", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (pinnedGoals.isNotEmpty()) {
                pinnedGoals.chunked(2).forEach { rowGoals ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowGoals.forEach { goal ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                shape  = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier              = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.Top
                                    ) {
                                        Text(goal.emoji, fontSize = 30.sp)
                                        // Edit + Delete buttons
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                "✏️",
                                                fontSize = 14.sp,
                                                modifier = Modifier.clickable { onStartEditing(goal) }
                                            )
                                            Text(
                                                "✕",
                                                color    = Color.Gray,
                                                fontSize = 14.sp,
                                                modifier = Modifier.clickable { onRemoveGoal(goal) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        goal.text,
                                        color      = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 13.sp,
                                        letterSpacing = 0.1.sp
                                    )
                                    if (goal.targetValue.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Target: ${goal.targetValue}",
                                            color    = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                        if (rowGoals.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VisionBoardPreview() {
    StudyOSTheme {
        VisionBoardContent(
            goalText        = "Score 95% in Finals",
            selectedEmoji   = "🏅",
            targetValue     = "95%",
            selectedSubject = "Math",
            subjects        = listOf("General", "Biology", "Physics", "Math"),
            pinnedGoals     = listOf(
                VisionGoalModel(text = "Finish Physics Lab", emoji = "🚀", targetValue = "100%"),
                VisionGoalModel(text = "Read Biology Ch 5",  emoji = "⭐")
            ),
            editingGoal         = null,
            onGoalTextChange    = {},
            onEmojiSelect       = {},
            onTargetValueChange = {},
            onSubjectSelect     = {},
            onAddSubject        = {},
            onPinGoal           = {},
            onRemoveGoal        = {},
            onStartEditing      = {},
            onEditGoal          = {},
            onStopEditing       = {},
            onBack              = {}
        )
    }
}