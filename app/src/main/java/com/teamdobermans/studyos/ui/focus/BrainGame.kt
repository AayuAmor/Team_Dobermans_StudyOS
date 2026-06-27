package com.teamdobermans.studyos.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.components.StudyOSPrimaryButton
import com.teamdobermans.studyos.ui.navigation.AppRoutes
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleFaint
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight
import com.teamdobermans.studyos.ui.theme.TextPrimary
import com.teamdobermans.studyos.ui.theme.TextSecondary

@Composable
fun BrainGameScreen(
    mode: String = AppRoutes.BrainGame.MODE_HUB,
    onBack: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StudyPurpleFaint),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { BrainGameHeader(mode = mode, onBack = onBack) }
        when (mode) {
            AppRoutes.BrainGame.MODE_MEMORY_MATCH -> item { MemoryMatchModeCard() }
            AppRoutes.BrainGame.MODE_MATH_SPRINT -> item { MathSprintModeCard() }
            else -> item { BrainGamesHub() }
        }
    }
}

@Composable
private fun BrainGameHeader(mode: String, onBack: () -> Unit) {
    val title = when (mode) {
        AppRoutes.BrainGame.MODE_MEMORY_MATCH -> "Memory Match"
        AppRoutes.BrainGame.MODE_MATH_SPRINT -> "Math Sprint"
        else -> "Brain Games"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(StudyPurpleDeep, StudyPurple)))
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp)
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.clickable { onBack() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Short, focused breaks that keep your mind sharp",
                color = Color.White.copy(alpha = 0.76f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BrainGamesHub() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BrainGameInfoCard(
            icon = Icons.Rounded.GridView,
            title = "Memory Match",
            subtitle = "Match pairs and train recall during study breaks.",
            accent = Color(0xFFE04F7B)
        )
        BrainGameInfoCard(
            icon = Icons.Rounded.Calculate,
            title = "Math Sprint",
            subtitle = "Rapid arithmetic practice for a quick cognitive reset.",
            accent = Color(0xFF4B5BE0)
        )
    }
}

@Composable
private fun MemoryMatchModeCard() {
    ModeCard(icon = Icons.Rounded.GridView, title = "Memory Match", accent = Color(0xFFE04F7B)) {
        Text("Mode ready", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Use the Focus screen card to start this break activity. Scores can be connected to persistence later without another duplicate Activity.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun MathSprintModeCard() {
    ModeCard(icon = Icons.Rounded.Calculate, title = "Math Sprint", accent = Color(0xFF4B5BE0)) {
        Text("? + ? = ?", color = StudyPurpleDeep, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatLabel("Score", "0")
            StatLabel("Best", "0")
            StatLabel("Time", "30")
        }
        Spacer(modifier = Modifier.height(24.dp))
        StudyOSPrimaryButton(
            text = "Start Game",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModeCard(icon: ImageVector, title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun BrainGameInfoCard(icon: ImageVector, title: String, subtitle: String, accent: Color) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Icon(Icons.Rounded.SportsEsports, contentDescription = null, tint = StudyPurpleLight)
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = StudyPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBrainGameScreen() {
    StudyOSTheme { BrainGameScreen() }
}
