package com.teamdobermans.studyos.ui.profile

import com.teamdobermans.studyos.R

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.viewModel.ProfileUiState
import com.teamdobermans.studyos.viewModel.ProfileViewModel
import kotlinx.coroutines.delay

class ProfileActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ProfileBody(viewModel = profileViewModel, onBack = { finish() }) }
    }
}

fun initials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
    else name.take(2).uppercase()
}

@Composable
fun ProfileBody(
    viewModel: ProfileViewModel = ProfileViewModel(),
    onBack: () -> Unit = {}
) {
    val profile    by viewModel.profile.collectAsState()
    val uiState    by viewModel.uiState.collectAsState()
    val nameError  by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()

    val saveResult by viewModel.saveResult.collectAsState()

    val displayName = profile.name.ifEmpty { profile.email.substringBefore("@") }

    var editName        by remember(profile.name)  { mutableStateOf(profile.name) }
    var editEmail       by remember(profile.email) { mutableStateOf(profile.email) }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val isLoading = uiState is ProfileUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            delay(3000)
            viewModel.clearResult()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { onBack() }
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Profile",
                style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
                .background(StudyPurpleLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDD8FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (displayName.isNotEmpty()) initials(displayName) else "U",
                            color = StudyPurple,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        displayName.ifEmpty { "User" },
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile.email, color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Update preferences",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = nameError != null,
                        label = { Text("Display name", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple,
                            errorContainerColor     = Color(0xFFFFF0F0)
                        )
                    )
                    if (nameError != null) {
                        Text(
                            nameError!!,
                            color = Color(0xFFE53935),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = emailError != null,
                        label = { Text("Email address", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple,
                            errorContainerColor     = Color(0xFFFFF0F0)
                        )
                    )
                    if (emailError != null) {
                        Text(
                            emailError!!,
                            color = Color(0xFFE53935),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.updateProfile(editName, editEmail) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (uiState is ProfileUiState.Success) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", color = Color(0xFF388E3C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                (uiState as ProfileUiState.Success).message,
                                color = Color(0xFF388E3C),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (uiState is ProfileUiState.Error) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✕", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                (uiState as ProfileUiState.Error).message,
                                color = Color(0xFFE53935),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Password reset",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("New password", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple
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
                        label = { Text("Confirm password", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF0EEFF),
                            focusedContainerColor   = Color(0xFFF0EEFF),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor   = StudyPurple
                        )
                    )
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Passwords do not match",
                            color = Color(0xFFE53935),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { if (passwordsMatch) viewModel.updatePassword(newPassword) },
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
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    StudyOSTheme { ProfileBody(viewModel = ProfileViewModel(), onBack = {}) }
}
