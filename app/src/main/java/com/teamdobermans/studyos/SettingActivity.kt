package com.teamdobermans.studyos

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.ui.theme.StudyOSTheme
import com.teamdobermans.studyos.ui.theme.StudyPurple
import com.teamdobermans.studyos.ui.theme.StudyPurpleDeep
import com.teamdobermans.studyos.ui.theme.StudyPurpleLight

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SettingsBody() }
    }
}

@Composable
fun SettingsBody() {

    val context  = LocalContext.current
    val activity = context as Activity

    var offlineMode       by remember { mutableStateOf(true) }
    var weeklySummary     by remember { mutableStateOf(true) }
    var focusSounds       by remember { mutableStateOf(false) }
    var pinNotes          by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.clickable { activity.finish() }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Settings", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }

        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight)
                .verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding()
        ) {

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("App Preferences", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleRow("Offline mode",                offlineMode)   { offlineMode   = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Weekly Summary Notification",  weeklySummary) { weeklySummary = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Focus Sounds",                 focusSounds)  { focusSounds   = it }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    SettingsToggleRow("Pin Notes",                    pinNotes)     { pinNotes      = it }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF1A1A2E), fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = StudyPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    StudyOSTheme { SettingsBodyPreview() }
}

@Composable
private fun SettingsBodyPreview() {
    Column(modifier = Modifier.fillMaxSize().background(StudyPurple)) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.25f)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Settings", style = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold))
        }
        Column(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp)).background(StudyPurpleLight).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Preferences", color = StudyPurple, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Offline mode" to true, "Weekly Summary Notification" to true, "Focus Sounds" to false, "Pin Notes" to false).forEach { (label, checked) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                            Text(label, color = Color(0xFF1A1A2E), fontSize = 14.sp)
                            Switch(checked = checked, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudyPurple, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)))
                        }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}