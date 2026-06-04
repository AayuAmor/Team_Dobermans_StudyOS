package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class QuizScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizScreenUI()
        }
    }
}

@Composable
fun QuizScreenUI() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF3F1FB))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(230.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color(0xFF5B4DDB), Color(0xFF6A5AF9))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Back", color = Color.White, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(text = "Question 1 of 5", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Which Organelle produces energy?", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (index == 0) Color.White else Color(0xFF8D83FF))
                            )
                            if (index < 4) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            OptionCard("A", "Nucleus")
            OptionCard("B", "Mitochondria")
            OptionCard("C", "Ribosome")
            OptionCard("D", "Golgi")
        }
    }
}

@Composable
fun OptionCard(letter: String, answer: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 22.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEAE5FF)), contentAlignment = Alignment.Center) {
            Text(text = letter, color = Color(0xFF7B42FF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(18.dp))
        Text(text = answer, color = Color(0xFF4A4A4A), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuizScreenUI() {
    QuizScreenUI()
}
