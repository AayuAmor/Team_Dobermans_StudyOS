package com.example.computing38

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BrainGameScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            QuizUI()
        }
    }
}

@Composable
fun QuizUI() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F1FB))
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // TOP SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF5B4DDB),
                                Color(0xFF6A5AF9)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {

                Column {

                    // BACK BUTTON
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Back",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Question 1 of 5",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Which Organnelle produces energy ?",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // PROGRESS BAR
                    Row {

                        repeat(5) { index ->

                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (index == 0)
                                            Color.White
                                        else
                                            Color(0xFF8D83FF)
                                    )
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // OPTIONS
            OptionCard("A", "Nucleus")
            OptionCard("B", "Mitochondria")
            OptionCard("C", "Ribosome")
            OptionCard("D", "Golgi")
        }

        // BOTTOM NAVIGATION
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomItem(
                title = "Home",
                icon = Icons.Default.Home,
                selected = true
            )

            BottomItem(
                title = "Study",
                icon = Icons.Default.MenuBook,
                selected = false
            )

            BottomItem(
                title = "Plan",
                icon = Icons.Default.CalendarMonth,
                selected = false
            )

            BottomItem(
                title = "Progress",
                icon = Icons.Default.ShowChart,
                selected = false
            )

            BottomItem(
                title = "Settings",
                icon = Icons.Default.Settings,
                selected = false
            )
        }
    }
}

@Composable
fun OptionCard(
    letter: String,
    answer: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // CIRCLE LETTER
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAE5FF)),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = letter,
                color = Color(0xFF7B42FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = answer,
            color = Color(0xFF7B42FF),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BottomItem(
    title: String,
    icon: ImageVector,
    selected: Boolean
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected)
                        Color(0xFFE8E2FF)
                    else
                        Color.Transparent
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF7B42FF)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = Color(0xFF7B42FF),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuizUI() {
    QuizUI()
}}