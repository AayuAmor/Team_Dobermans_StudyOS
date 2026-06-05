package com.teamdobermans.studyos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.teamdobermans.studyos.ui.theme.BrandPurple
import com.teamdobermans.studyos.ui.theme.LightPurpleBg

data class NavTab(
    val route:        String,
    val label:        String,
    val selectedIcon: ImageVector,
    val defaultIcon:  ImageVector
)

val bottomNavTabs = listOf(
    NavTab(
        route        = AppRoutes.Home.route,
        label        = "Home",
        selectedIcon = Icons.Rounded.Home,
        defaultIcon  = Icons.Outlined.Home
    ),
    NavTab(
        route        = AppRoutes.Study.route,
        label        = "Study",
        selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
        defaultIcon  = Icons.AutoMirrored.Outlined.MenuBook
    ),
    NavTab(
        route        = AppRoutes.Focus.route,
        label        = "Focus",
        selectedIcon = Icons.Rounded.Timer,
        defaultIcon  = Icons.Outlined.Timer
    ),
    NavTab(
        route        = AppRoutes.Plan.route,
        label        = "Plan",
        selectedIcon = Icons.AutoMirrored.Rounded.EventNote,
        defaultIcon  = Icons.AutoMirrored.Outlined.EventNote
    ),
    NavTab(
        route        = AppRoutes.Profile.route,
        label        = "Profile",
        selectedIcon = Icons.Rounded.Person,
        defaultIcon  = Icons.Outlined.Person
    )
)

val bottomNavRoutes: Set<String> = bottomNavTabs.map { it.route }.toSet()

@Composable
fun StudyOSBottomNav(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        bottomNavTabs.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector     = if (isSelected) tab.selectedIcon else tab.defaultIcon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text       = tab.label,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = isSelected,
                onClick  = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(AppRoutes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = BrandPurple,
                    selectedTextColor   = BrandPurple,
                    indicatorColor      = LightPurpleBg,
                    unselectedIconColor = BrandPurple.copy(alpha = 0.45f),
                    unselectedTextColor = BrandPurple.copy(alpha = 0.45f)
                )
            )
        }
    }
}

