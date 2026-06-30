package com.teamdobermans.studyos.ui.navigation

sealed class AppRoutes(val route: String) {

    object Auth : AppRoutes("auth")
    object Login : AppRoutes("login")
    object SignUp : AppRoutes("signup")

    object Home : AppRoutes("home")
    object Study : AppRoutes("study")
    object Focus : AppRoutes("focus")
    object Plan : AppRoutes("plan")
    object Profile : AppRoutes("profile")

    object Notes : AppRoutes("notes")
    object Analytics : AppRoutes("analytics")
    object Flashcards : AppRoutes("flashcards")
    object BrainGame : AppRoutes("braingame/{mode}") {
        const val MODE_HUB = "hub"
        const val MODE_MEMORY_MATCH = "memory_match"
        const val MODE_MATH_SPRINT = "math_sprint"
        fun route(mode: String = MODE_HUB): String = "braingame/$mode"
    }

    object VideoNotes : AppRoutes("videonotes")
    object MockTest : AppRoutes("mocktest")
    object VisionBoard : AppRoutes("visionboard")
    object Pomodoro : AppRoutes("pomodoro")
    object Settings : AppRoutes("settings")

}
