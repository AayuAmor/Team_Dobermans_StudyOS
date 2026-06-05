package com.teamdobermans.studyos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.teamdobermans.studyos.ui.theme.*
import com.teamdobermans.studyos.viewModel.AuthState
import com.teamdobermans.studyos.viewModel.AuthViewModel

class SignUpActivity : ComponentActivity() {

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account    = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Google Sign-In failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                SignUpBody(
                    viewModel       = AuthViewModel(),
                    onSignUpSuccess = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    onBack         = { finish() },
                    onGoogleSignIn = { googleLauncher.launch(GoogleSignInHelper.getSignInIntent(this)) }
                )
            }
        }
    }
}

@Composable
fun SignUpBody(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit = {}
) {
    var fullName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onSignUpSuccess()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(StudyPurpleDeep).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { onBack() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Create Account", style = TextStyle(color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(18.dp, RoundedCornerShape(20.dp)),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Full Name", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = StudyPurpleLight, focusedIndicatorColor = StudyPurple,
                    cursorColor = StudyPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Email", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = StudyPurpleLight, focusedIndicatorColor = StudyPurple,
                    cursorColor = StudyPurple
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Password", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.4.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = if (passwordVisible) painterResource(R.drawable.baseline_visibility_24) else painterResource(R.drawable.baseline_visibility_off_24),
                                contentDescription = null
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = StudyPurpleLight, focusedIndicatorColor = StudyPurple,
                    cursorColor = StudyPurple
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick  = { if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) viewModel.signUp(email, password) else Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth().height(50.dp).shadow(6.dp, RoundedCornerShape(25.dp)),
                    shape    = RoundedCornerShape(25.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = StudyPurple),
                    enabled  = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Sign Up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                    Text(" or continue with ", color = TextHint, fontSize = 11.sp, letterSpacing = 0.3.sp, modifier = Modifier.padding(horizontal = 6.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick  = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth().height(50.dp).shadow(6.dp, RoundedCornerShape(25.dp)),
                    shape    = RoundedCornerShape(25.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Image(painter = painterResource(R.drawable.google), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google", color = Color.DarkGray, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                    Text("Sign In", color = StudyPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onBack() })
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
fun SignUpPreview() {
    StudyOSTheme {
        SignUpBody(viewModel = AuthViewModel(), onSignUpSuccess = {}, onBack = {}, onGoogleSignIn = {})
    }
}
