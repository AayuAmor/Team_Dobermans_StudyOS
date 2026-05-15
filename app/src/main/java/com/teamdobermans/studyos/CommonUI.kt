package com.teamdobermans.studyos

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.remember
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.ui.theme.StudyPurple

@Composable
fun StudyBottomNav(selected: Int) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Home",
                icon = if (selected == 0) Icons.Rounded.Home else Icons.Outlined.Home,
                isSelected = selected == 0,
                onClick = { /* TODO: Navigate to Home */ }
            )
            NavItem(
                label = "Study",
                icon = if (selected == 1) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook,
                isSelected = selected == 1,
                onClick = {
                    if (selected != 1) {
                        context.startActivity(Intent(context, VisionBoardActivity::class.java))
                    }
                }
            )
            NavItem(
                label = "Plan",
                icon = if (selected == 2) Icons.Rounded.EventNote else Icons.Outlined.EventNote,
                isSelected = selected == 2,
                onClick = { /* TODO: Navigate to Plan */ }
            )
            NavItem(
                label = "Progress",
                icon = if (selected == 3) Icons.Rounded.BarChart else Icons.Outlined.BarChart,
                isSelected = selected == 3,
                onClick = { /* TODO: Navigate to Progress */ }
            )
            NavItem(
                label = "Settings",
                icon = if (selected == 4) Icons.Rounded.Settings else Icons.Outlined.Settings,
                isSelected = selected == 4,
                onClick = {
                    if (selected != 4) {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }
                }
            )
        }
    }
}

@Composable
fun NavItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val tintColor = if (isSelected) StudyPurple else Color(0xFF8E8EA9)
    val bgColor = if (isSelected) StudyPurple.copy(alpha = 0.12f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}


/**
 * Extension function to safely find the Activity from a Context.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
