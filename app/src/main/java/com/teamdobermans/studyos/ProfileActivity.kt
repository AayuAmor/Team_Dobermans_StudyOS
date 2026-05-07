package com.teamdobermans.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.*

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Scaffold(
        bottomBar = {

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F9))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudyPurple)
                    .padding(bottom = 60.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            onClick = { /* Back */ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    Text(
                        "Profile",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            // Main Content with negative offset for overlap
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-40).dp)
            ) {
                // Profile Info Card



                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = StudyPurpleLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "DA",
                                    color = StudyPurple,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Ditya Adhikari",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudyPurple
                        )
                        Text(
                            "dityaadhikari345@gmail.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Update Preferences Card
                PreferenceCard(
                    title = "Update preferences",
                    fields = listOf(
                        "Ditya Adhikari" to "Name",
                        "" to "Email"
                    ),
                    buttonText = "Save Changes"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password Reset Card
                PreferenceCard(
                    title = "Password reset",
                    fields = listOf(
                        "" to "New password",
                        "" to "Confirm password"
                    ),
                    buttonText = "Update password"
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PreferenceCard(title: String, fields: List<Pair<String, String>>, buttonText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                title,
                color = StudyPurple.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            fields.forEach { (value, placeholder) ->
                ProfileTextField(value = value, placeholder = placeholder)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileTextField(value: String, placeholder: String) {
    TextField(
        value = value,
        onValueChange = {},
        placeholder = { Text(placeholder, color = StudyPurple.copy(alpha = 0.4f), fontWeight = FontWeight.Bold) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = StudyPurpleLight,
            focusedContainerColor = StudyPurpleLight,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = StudyPurple,
            unfocusedTextColor = StudyPurple
        ),
        singleLine = true
    )
}



@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    StudyOSTheme {
        ProfileScreen()
    }
}
