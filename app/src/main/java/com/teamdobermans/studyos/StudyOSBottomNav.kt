package com.teamdobermans.studyos

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg

data class NavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

val bottomNavItems = listOf(
    NavItem(AppRoutes.Dashboard.route,  "Home",     R.drawable.baseline_home_24),
    NavItem(AppRoutes.Notes.route,      "Study",    R.drawable.baseline_menu_book_24),
    NavItem(AppRoutes.Plan.route,       "Plan",     R.drawable.ic_calendar),
    NavItem(AppRoutes.Progress.route,   "Progress", R.drawable.ic_progress),
    NavItem(AppRoutes.Settings.route,   "Settings", R.drawable.baseline_settings_24)
)

@Composable
fun StudyOSBottomNav(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon     = { Icon(painterResource(item.iconRes), contentDescription = null) },
                label    = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                selected = currentRoute == item.route,
                onClick  = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(AppRoutes.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                colors = navItemColors()
            )
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = BrandPurple,
    selectedTextColor   = BrandPurple,
    indicatorColor      = LightPurpleBg,
    unselectedIconColor = BrandPurple.copy(alpha = 0.45f),
    unselectedTextColor = BrandPurple.copy(alpha = 0.45f)
)
