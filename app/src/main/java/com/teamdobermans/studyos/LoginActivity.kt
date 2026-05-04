package com.teamdobermans.studyos

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.tooling.preview.Preview

import com.teamdobermans.studyos.ui.theme.*

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody() {

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe      by remember { mutableStateOf(false) }

    val context  = LocalContext.current
    val activity = context as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.clickable { activity.finish() }
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Welcome Back",
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(18.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudyCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    "Email",
                    color = StudyPurple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(color = Color.White, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor   = StudyPurple,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Password",
                    color = StudyPurple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(color = Color.White, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = if (passwordVisible)
                                    painterResource(R.drawable.baseline_visibility_24)
                                else
                                    painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = null
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor   = StudyPurple,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            "Remember me",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        "Forgot Password?",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { /* TODO: Forgot password */ }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* sign-in logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(6.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Text(
                        "Sign In",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                    Text(
                        " or ",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Google auth */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(6.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Continue with Google",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Don't have an account? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        "Sign up",
                        color = StudyPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
//                            val intent = Intent(context, SignUpActivity::class.java)
//                            context.startActivity(intent)
                        }
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(32.dp))

    }
}



@Composable
private fun LoginBodyPreview() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyPurpleDeep)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.25f)
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Welcome Back",
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(18.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudyCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    "Email",
                    color = StudyPurple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(color = Color.White, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = StudyPurple,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Password",
                    color = StudyPurple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(color = Color.White, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = null
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = StudyPurple,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = {}
                        )
                        Text(
                            "Remember me",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        "Forgot Password?",
                        color = StudyPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(6.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPurple)
                ) {
                    Text(
                        "Sign In",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                    Text(
                        " or ",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginBodyPreview() {
    LoginBody()
}





