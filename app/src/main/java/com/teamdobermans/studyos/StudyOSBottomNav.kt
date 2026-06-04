package com.teamdobermans.studyos

import android.content.Context
import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg

enum class NavRoute {
    HOME, STUDY, PLAN, PROGRESS, SETTINGS
}

@Composable
fun StudyOSBottomNav(currentRoute: NavRoute, context: Context) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {

        NavigationBarItem(
            icon    = { Icon(painterResource(R.drawable.baseline_home_24), contentDescription = null) },
            label   = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == NavRoute.HOME,
            onClick = {
                if (currentRoute != NavRoute.HOME) {
                    context.startActivity(Intent(context, DashboardActivity::class.java))
                }
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            icon    = { Icon(painterResource(R.drawable.baseline_menu_book_24), contentDescription = null) },
            label   = { Text(stringResource(R.string.nav_study)) },
            selected = currentRoute == NavRoute.STUDY,
            onClick = {
                if (currentRoute != NavRoute.STUDY) {
                    context.startActivity(Intent(context, NotesPage::class.java))
                }
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            icon    = { Icon(painterResource(R.drawable.ic_calendar), contentDescription = null) },
            label   = { Text(stringResource(R.string.nav_plan)) },
            selected = currentRoute == NavRoute.PLAN,
            onClick = {
                if (currentRoute != NavRoute.PLAN) {
                    context.startActivity(Intent(context, PlanActivity::class.java))
                }
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            icon    = { Icon(painterResource(R.drawable.ic_progress), contentDescription = null) },
            label   = { Text(stringResource(R.string.nav_progress)) },
            selected = currentRoute == NavRoute.PROGRESS,
            onClick = {
                if (currentRoute != NavRoute.PROGRESS) {
                    context.startActivity(Intent(context, ProgressActivity::class.java))
                }
            },
            colors = navItemColors()
        )

        NavigationBarItem(
            icon    = { Icon(painterResource(R.drawable.baseline_settings_24), contentDescription = null) },
            label   = { Text(stringResource(R.string.nav_settings)) },
            selected = currentRoute == NavRoute.SETTINGS,
            onClick = {
                if (currentRoute != NavRoute.SETTINGS) {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor    = BrandPurple,
    selectedTextColor    = BrandPurple,
    indicatorColor       = LightPurpleBg,
    unselectedIconColor  = BrandPurple.copy(alpha = 0.45f),
    unselectedTextColor  = BrandPurple.copy(alpha = 0.45f)
)