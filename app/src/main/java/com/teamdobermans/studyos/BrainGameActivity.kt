package com.example.dobermans_team_studyos
import androidx.compose.ui.tooling.preview.Preview
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BrainGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BrainGameBody()
        }
    }
}

@Composable
fun BrainGameBody() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F1FB))
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
                                Color(0xFF5B4DDB),
                                Color(0xFF6A5AF9)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {

                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Brain Games",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Suggested after Pomodoro",
                            color = Color.White,
                            fontSize = 18.sp
                        )

                        Text(
                            text = "• Track scores",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))

            // TITLE
            Text(
                text = "Mental math sprint",
                color = Color(0xFF5B4DDB),
                fontSize = 32.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // QUESTION
            Text(
                text = "? + ? = ?",
                color = Color(0xFF352C8B),
                fontSize = 62.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Preview
@Composable
fun BrainGamePreview(){
    BrainGameBody()
}