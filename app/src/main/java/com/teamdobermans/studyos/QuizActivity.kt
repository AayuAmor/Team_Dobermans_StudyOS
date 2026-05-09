package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizUI()

        }
    }
}

@Composable
fun QuizUI(){

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F1FB))
        ) {

            Column (
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
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

                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {

                            Image(
                                painter = painterResource(R.drawable.outline_arrow_back_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Back",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Question 1 of 5",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Which Organnelle produces energy ?",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))


                        Row {


                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            repeat(4) {

                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF8E84FF))
                                )

                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(45.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0EBFF)),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "A",
                            color = Color(0xFF7B42FF),
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = "Nucleus",
                        color = Color(0xFF7B42FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0EBFF)),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "B",
                            color = Color(0xFF7B42FF),
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = "Mitochondria",
                        color = Color(0xFF7B42FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0EBFF)),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "C",
                            color = Color(0xFF7B42FF),
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = "Ribosome",
                        color = Color(0xFF7B42FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0EBFF)),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "D",
                            color = Color(0xFF7B42FF),
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = "Golgi",
                        color = Color(0xFF7B42FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }




@Preview
@Composable
fun PreviewQuizUI(){
    QuizUI()
}
