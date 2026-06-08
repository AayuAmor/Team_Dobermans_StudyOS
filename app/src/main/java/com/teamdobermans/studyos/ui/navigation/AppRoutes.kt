package com.teamdobermans.studyos.ui.navigation

sealed class AppRoutes(val route: String) {

    object Auth        : AppRoutes("auth")
    object Login       : AppRoutes("login")
    object SignUp      : AppRoutes("signup")

    object Home        : AppRoutes("home")
    object Study       : AppRoutes("study")
    object Focus       : AppRoutes("focus")
    object Plan        : AppRoutes("plan")
    object Profile     : AppRoutes("profile")

    object Notes       : AppRoutes("notes")
    object Analytics   : AppRoutes("analytics")
    object Flashcards  : AppRoutes("flashcards")
    object BrainGame   : AppRoutes("braingame")
    object VideoNotes  : AppRoutes("videonotes")
    object MockTest    : AppRoutes("mocktest")
    object VisionBoard : AppRoutes("visionboard")
    object Pomodoro    : AppRoutes("pomodoro")
    object Settings    : AppRoutes("settings")

}

