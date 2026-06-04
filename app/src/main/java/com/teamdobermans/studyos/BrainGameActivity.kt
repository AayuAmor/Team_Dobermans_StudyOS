package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

class BrainGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BrainGameBody() }
    }
}

@Composable
fun BrainGameBody() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F1FB))) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color(0xFF5B4DDB), Color(0xFF6A5AF9))))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Back", color = Color.White, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    Text(text = "Brain Games", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Suggested after Pomodoro", color = Color.White, fontSize = 18.sp)
                        Text(text = "Track scores", color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
            Text(text = "Mental math sprint", color = Color(0xFF5B4DDB), fontSize = 32.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(60.dp))
            Text(text = "? + ? = ?", color = Color(0xFF352C8B), fontSize = 62.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(100.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Score", color = Color(0xFF5B4DDB), fontSize = 20.sp); Spacer(modifier = Modifier.height(12.dp)); Text("0", color = Color(0xFF5B4DDB), fontSize = 30.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Best", color = Color(0xFF5B4DDB), fontSize = 20.sp); Spacer(modifier = Modifier.height(12.dp)); Text("0", color = Color(0xFF5B4DDB), fontSize = 30.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Time", color = Color(0xFF5B4DDB), fontSize = 20.sp); Spacer(modifier = Modifier.height(12.dp)); Text("30", color = Color.Red, fontSize = 30.sp) }
            }

            Spacer(modifier = Modifier.height(70.dp))
            Button(
                onClick  = {},
                modifier = Modifier.align(Alignment.CenterHorizontally).width(220.dp).height(58.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4DDB))
            ) { Text("Start Game", color = Color.White, fontSize = 24.sp) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BrainGamePreview() {
    BrainGameBody()
}
