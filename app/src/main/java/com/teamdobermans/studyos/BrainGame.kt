package com.teamdobermans.studyos

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
            MaterialTheme {
                BrainUI()
            }
        }
    }
}

@Composable
fun BrainUI() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5FB))
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // TOP HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF5F4BE3),
                                Color(0xFF6D57F5)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // BACK BUTTON
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Back",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Brain Games",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Suggested after Pomodoro",
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "• Track scores",
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, bottom = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Mental math sprint",
                        color = Color(0xFF5A4BD8),
                        fontSize = 23.sp
                    )

                    Spacer(modifier = Modifier.height(45.dp))

                    Text(
                        text = "? + ? = ?",
                        color = Color(0xFF352C8B),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(70.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // SCORE
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Score",
                                color = Color(0xFF5A4BD8),
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "0",
                                color = Color(0xFF5A4BD8),
                                fontSize = 24.sp
                            )
                        }

                        // BEST
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Best",
                                color = Color(0xFF5A4BD8),
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "0",
                                color = Color(0xFF5A4BD8),
                                fontSize = 24.sp
                            )
                        }

                        // TIME
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Time",
                                color = Color(0xFF5A4BD8),
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "30",
                                color = Color.Red,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(55.dp))

                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5A4BD8)
                        ),
                        modifier = Modifier
                            .width(190.dp)
                            .height(56.dp)
                    ) {

                        Text(
                            text = "Start Game",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }

        // BOTTOM NAVIGATION
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 12.dp),
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
fun PreviewBrainUI() {
    MaterialTheme {
        BrainUI()
    }
}