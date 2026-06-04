package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

class BrainGameActivityShell : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { BrainGameScreen(onBack = { finish() }) }
        }
    }
}

@Composable
fun BrainGameScreen(onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5FB))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color(0xFF5F4BE3), Color(0xFF6D57F5))))
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onBack() }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Back", color = Color.White, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(text = "Brain Games", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Suggested after Pomodoro", color = Color.White, fontSize = 15.sp)
                        Text(text = "Track scores", color = Color.White, fontSize = 15.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(text = "Mental math sprint", color = Color(0xFF5A4BD8), fontSize = 23.sp)
                    Spacer(modifier = Modifier.height(45.dp))
                    Text(text = "? + ? = ?", color = Color(0xFF352C8B), fontSize = 46.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(70.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Score", color = Color(0xFF5A4BD8), fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("0", color = Color(0xFF5A4BD8), fontSize = 24.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Best", color = Color(0xFF5A4BD8), fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("0", color = Color(0xFF5A4BD8), fontSize = 24.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Time", color = Color(0xFF5A4BD8), fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("30", color = Color.Red, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(55.dp))

                    Button(
                        onClick  = {},
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A4BD8)),
                        modifier = Modifier.width(190.dp).height(56.dp)
                    ) {
                        Text("Start Game", color = Color.White, fontSize = 17.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBrainGameScreen() {
    MaterialTheme { BrainGameScreen() }
}
