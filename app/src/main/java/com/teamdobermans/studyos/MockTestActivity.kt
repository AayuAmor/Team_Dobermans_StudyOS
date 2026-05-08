package com.teamdobermans.studyos
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateListOf
import com.teamdobermans.studyos.ui.theme.StudyOSTheme

private enum class Difficulty { EASY, MEDIUM, HARD }

class MockTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MockTestBody()
        }
    }
}

@Composable
fun MockTestBody() {

    val context  = LocalContext.current
    val activity = context as Activity

    val subjects         = remember { mutableStateListOf("All Subjects") }
    var selectedSubject  by remember { mutableStateOf("All Subjects") }
    var subjectDropdown  by remember { mutableStateOf(false) }
    var addSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectInput  by remember { mutableStateOf("") }

    val durationSteps = listOf(10, 30, 60, 90, 120)
    var durationIndex by remember { mutableStateOf(1) }

    val questionSteps = listOf(5, 15, 25, 35, 50)
    var questionIndex by remember { mutableStateOf(1) }

    var difficulty      by remember { mutableStateOf(Difficulty.MEDIUM) }
    val notes           = remember { mutableStateListOf<String>() }
    var selectedNote    by remember { mutableStateOf("None") }
    var notesDropdown   by remember { mutableStateOf(false) }

    if (addSubjectDialog) {
        AlertDialog(
            onDismissRequest = { addSubjectDialog = false; newSubjectInput = "" },
            title = { Text("Add Subject", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)) },
            text = {
                OutlinedTextField(
                    value = newSubjectInput,
                    onValueChange = { newSubjectInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g. Biology, Physics", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F3FF),
                        focusedContainerColor   = Color(0xFFF5F3FF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor   = StudyPurple,
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newSubjectInput.trim()
                        if (trimmed.isNotEmpty() && !subjects.contains(trimmed)) {
                            subjects.add(trimmed)
                            selectedSubject = trimmed
                        }
                        addSubjectDialog = false
                        newSubjectInput = ""
                    },
                    enabled = newSubjectInput.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { addSubjectDialog = false; newSubjectInput = "" }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurple)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { activity.finish() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Mock Test", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Timed exam simulation", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
                .background(StudyPurpleLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Mock Test", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StudyPurple,
                            modifier = Modifier.clickable { subjectDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedSubject, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(painter = painterResource(R.drawable.baseline_more_horiz_24),
                                    contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = subjectDropdown, onDismissRequest = { subjectDropdown = false }) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = {
                                        Text(subject,
                                            fontWeight = if (subject == selectedSubject) FontWeight.Bold else FontWeight.Normal,
                                            color = if (subject == selectedSubject) StudyPurple else Color(0xFF1A1A2E))
                                    },
                                    onClick = { selectedSubject = subject; subjectDropdown = false }
                                )
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(painter = painterResource(R.drawable.baseline_notifications_none_24),
                                            contentDescription = null, tint = StudyPurple, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add new subject...", color = StudyPurple, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = { subjectDropdown = false; addSubjectDialog = true }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Duration", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${durationSteps[durationIndex]}", color = Color(0xFF1A1A2E), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("min", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    }
                    Slider(
                        value = durationIndex.toFloat(),
                        onValueChange = { durationIndex = it.toInt() },
                        valueRange = 0f..4f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = StudyPurple, activeTrackColor = StudyPurple, inactiveTrackColor = Color(0xFFD0CBFF))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        durationSteps.forEach { step -> Text("${step}min", fontSize = 11.sp, color = Color.Gray) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Questions", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${questionSteps[questionIndex]}", color = Color(0xFF1A1A2E), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("questions", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                    }
                    Slider(
                        value = questionIndex.toFloat(),
                        onValueChange = { questionIndex = it.toInt() },
                        valueRange = 0f..4f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(thumbColor = StudyPurple, activeTrackColor = StudyPurple, inactiveTrackColor = Color(0xFFD0CBFF))
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        questionSteps.forEach { step -> Text("$step", fontSize = 11.sp, color = Color.Gray) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Generate from Notes", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "AI will use your selected note to generate questions",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedNote == "None")
                                Color.Gray.copy(alpha = 0.15f) else StudyPurple,
                            modifier = Modifier.clickable { notesDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_history_24),
                                    contentDescription = null,
                                    tint = if (selectedNote == "None") Color.Gray else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (selectedNote == "None") "Select a note" else selectedNote,
                                    color = if (selectedNote == "None") Color.Gray else Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(R.drawable.baseline_more_horiz_24),
                                    contentDescription = null,
                                    tint = if (selectedNote == "None") Color.Gray else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(expanded = notesDropdown, onDismissRequest = { notesDropdown = false }) {

                            DropdownMenuItem(
                                text = {
                                    Text("None",
                                        fontWeight = if (selectedNote == "None") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedNote == "None") StudyPurple else Color(0xFF1A1A2E))
                                },
                                onClick = {
                                    selectedNote = "None"
                                    notesDropdown = false
                                }
                            )

                            if (notes.isEmpty()) {
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "No notes added yet",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = { notesDropdown = false },
                                    enabled = false
                                )
                            } else {
                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                                notes.forEach { note ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(note,
                                                fontWeight = if (note == selectedNote) FontWeight.Bold else FontWeight.Normal,
                                                color = if (note == selectedNote) StudyPurple else Color(0xFF1A1A2E))
                                        },
                                        onClick = {
                                            selectedNote = note
                                            // Mirror the note's subject into the subject dropdown
                                            if (subjects.contains(note)) {
                                                selectedSubject = note
                                            }
                                            notesDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedNote != "None") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0EEFF)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Subject set to: ", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    if (subjects.contains(selectedNote)) selectedNote else "All Subjects",
                                    color = StudyPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("DIFFICULTY", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DifficultyChip(label = "Easy",   selected = difficulty == Difficulty.EASY,
                    selectedColor = Color(0xFF4CAF50), bgColor = Color(0xFFE8F5E9), modifier = Modifier.weight(1f)) { difficulty = Difficulty.EASY }
                DifficultyChip(label = "Medium", selected = difficulty == Difficulty.MEDIUM,
                    selectedColor = Color(0xFFFFC107), bgColor = Color(0xFFFFF8E1), modifier = Modifier.weight(1f)) { difficulty = Difficulty.MEDIUM }
                DifficultyChip(label = "Hard",   selected = difficulty == Difficulty.HARD,
                    selectedColor = Color(0xFFE53935), bgColor = Color(0xFFFFEBEB), modifier = Modifier.weight(1f)) { difficulty = Difficulty.HARD }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
            ) {
                Text("Start Test", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
//        StudyBottomNav(selected = 0)
//    }
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
        shape = RoundedCornerShape(50.dp),
        color = bgColor,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (selected) selectedColor else Color.Gray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MockTestBodyPreview() {

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Mock Test", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
            Text("Timed exam simulation", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
        }

        Column(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight).padding(16.dp)) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mock Test", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = StudyPurple) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("All Subjects", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Generate from Notes", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI will use your selected note to generate questions", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color.Gray.copy(alpha = 0.15f)) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(R.drawable.baseline_history_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select a note", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("DIFFICULTY", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Triple("Easy", Color(0xFF4CAF50), Color(0xFFE8F5E9)),
                    Triple("Medium", Color(0xFFFFC107), Color(0xFFFFF8E1)),
                    Triple("Hard", Color(0xFFE53935), Color(0xFFFFEBEB))).forEach { (label, sel, bg) ->
                    Surface(shape = RoundedCornerShape(50.dp), color = bg, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(label, color = if (label == "Medium") sel else Color.Gray,
                                fontWeight = if (label == "Medium") FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)) {
                Text("Start Test", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MockTestPreview() {
    StudyOSTheme {
        MockTestBodyPreview()
    }
}