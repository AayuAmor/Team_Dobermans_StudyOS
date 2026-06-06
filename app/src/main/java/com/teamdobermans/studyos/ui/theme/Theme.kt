package com.teamdobermans.studyos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudyOSColorScheme = lightColorScheme(
    primary          = StudyPurple,
    onPrimary        = Color.White,
    primaryContainer = StudyPurpleLight,
    secondary        = PurpleGrey40,
    tertiary         = Pink40,
    background       = StudyPurpleFaint,
    surface          = StudyCardBg,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary
)

@Composable
fun StudyOSTheme(
    darkTheme: Boolean    = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyOSColorScheme,
        typography  = Typography,
        content     = content
    )
}

