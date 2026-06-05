package com.teamdobermans.studyos.ui.onboarding
import com.teamdobermans.studyos.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule

class OnboardingActivity2_StudyActivityOS : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyUI()
        }
    }
}

@Composable
fun StudyUI(
    onNavigateMockTest: () -> Unit = {},
    onNavigateFlashcard: () -> Unit = {},
    onNavigateBrainGame: () -> Unit = {},
    onNavigateVideoNotes: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF6A5AE0), Color(0xFF5A4FCF)))
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(20.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.studyoslogo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Key Features", fontSize = 24.sp, color = Color.White)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.15f))
                    .clickable { onNavigateFlashcard() }.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Flashcards & Quiz", color = Color.White)
                    Text("Spaced repetition learning", color = Color.LightGray, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.15f))
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Pomodoro Time", color = Color.White)
                    Text("Customizable focus sessions", color = Color.LightGray, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.15f))
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Smart Notes", color = Color.White)
                    Text("Organize in folder, search instantly", color = Color.LightGray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(RoundedCornerShape(50)).background(Color.LightGray))
                Box(modifier = Modifier.padding(4.dp).width(40.dp).height(8.dp).clip(RoundedCornerShape(50)).background(Color.White))
                Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(RoundedCornerShape(50)).background(Color.LightGray))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick  = {},
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape    = RoundedCornerShape(15.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6D1E8))
            ) {
                Text("Next", color = Color.DarkGray)
            }
        }
    }
}

@Preview
@Composable
fun PreviewStudyUI() {
    StudyUI()
}

