package com.example.dobermans_team_studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

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

    }
}

@Preview
@Composable
fun BrainGamePreview(){
    BrainGameBody()
}