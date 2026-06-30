package com.teamdobermans.studyos.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.teamdobermans.studyos.ui.auth.LoginActivity
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.viewModel.SettingsViewModel

class ProfileActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyOSTheme {
                SettingsBody(
                    viewModel = settingsViewModel,
                    onSignOut = {
                        startActivity(
                            Intent(this, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                    }
                )
            }
        }
    }
}
