package com.teamdobermans.studyos

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ProfileBody() }
    }
}

fun initials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
    else name.take(2).uppercase()
}

@Composable
fun ProfileBody() {

    val context  = LocalContext.current
    val activity = context as? Activity

    val displayName  = "Ditya Adhikari"
    val displayEmail = "dityaadhikari345@gmail.com"

    var editName        by remember { mutableStateOf(displayName) }
    var editEmail       by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { activity?.finish() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Profile", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()
        ) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFDDD8FF)), contentAlignment = Alignment.Center) {
                        Text(initials(displayName), color = StudyPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(displayName, color = Color(0xFF1A1A2E), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(displayEmail, color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Update preferences", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF), unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        placeholder = { Text("Email", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0EEFF), focusedContainerColor = Color(0xFFF0EEFF), unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = StudyPurple))
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { /* TODO: save changes */ }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)) {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Password reset", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        placeholder = { Text("New password", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple,
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                        placeholder = { Text("Confirm password", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple,
                        )
                    )

                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Passwords do not match", color = Color(0xFFE53935), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { /* TODO: update password */ },
                        enabled = passwordsMatch,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                    ) {
                        Text("Update password", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
//        StudyBottomNav(selected = 0)
//    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    StudyOSTheme {
        ProfileBody()
    }
}