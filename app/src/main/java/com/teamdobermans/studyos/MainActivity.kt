package com.teamdobermans.studyos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.ui.navigation.AppRoutes
import com.teamdobermans.studyos.ui.navigation.StudyOSNavGraph
import com.teamdobermans.studyos.ui.onboarding.OnboardingActivity1_WelcomePage
import com.teamdobermans.studyos.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("onboarding_completed", false)) {
            startActivity(Intent(this, OnboardingActivity1_WelcomePage::class.java))
            finish()
            return
        }
        val startDestination = AppRoutes.Home.route
        setContent {
            StudyOSTheme {
                StudyOSNavGraph(startDestination = startDestination)
            }
        }
    }
}

@Composable
fun AuthScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.size(130.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(R.drawable.logo), contentDescription = "Logo")
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append("Let's build the\nsystem ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF4DD9E8), fontWeight = FontWeight.Bold)) {
                        append("Together")
                    }
                },
                fontSize = 35.sp,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(44.dp))

            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(30.dp)
                    ),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "Sign Up",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(30.dp)
                    ),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "Sign In",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun AuthScreenPreview() {
    AuthScreen(onSignUpClick = {}, onSignInClick = {})
}

