package com.teamdobermans.studyos.ui.onboarding
import com.teamdobermans.studyos.R

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import com.teamdobermans.studyos.ui.auth.LoginActivity
import com.teamdobermans.studyos.ui.auth.SignUpActivity
import com.teamdobermans.studyos.ui.theme.*

class OnboardingActivity3_worksOffline : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnboardingBody()
        }
    }
}

@Composable
fun OnboardingBody() {

    val context  = LocalContext.current

    val features = listOf(
        "Study anywhere, anytime",
        "Notes sync when back online",
        "Offline flashcards & quiz",
        "Auto-save everything"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.size(110.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.studyos_logo),
                contentDescription = "StudyOS",
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(18.dp)
                    .size(74.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "StudyOS",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Work Offline",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_check_24),
                            contentDescription = null,
                            tint = StudyPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            feature,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("onboarding_completed", true).apply()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                "Create Account",
                color = StudyPurpleDeep,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Already have an account? ",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
            Text(
                "Sign In",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("onboarding_completed", true).apply()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            )
        }

    }
}

@Composable
private fun OnboardingBodyPreview() {

    val features = listOf(
        "Study anywhere, anytime",
        "Notes sync when back online",
        "Offline flashcards & quiz",
        "Auto-save everything"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.size(110.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.studyos_logo),
                contentDescription = "StudyOS",
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(18.dp)
                    .size(74.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("StudyOS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        Text("Work Offline", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_check_24),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            feature,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            )

        ) {
            Text(
                "Create Account",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    StudyOSTheme {
        OnboardingBodyPreview()
    }
}
