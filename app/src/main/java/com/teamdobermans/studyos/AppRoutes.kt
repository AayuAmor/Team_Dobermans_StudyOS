package com.teamdobermans.studyos

sealed class AppRoutes(val route: String) {
    object Auth        : AppRoutes("auth")
    object Login       : AppRoutes("login")
    object SignUp      : AppRoutes("signup")
    object Dashboard   : AppRoutes("dashboard")
    object Notes       : AppRoutes("notes")
    object Plan        : AppRoutes("plan")
    object Progress    : AppRoutes("progress")
    object Settings    : AppRoutes("settings")
    object Pomodoro    : AppRoutes("pomodoro")
    object MockTest    : AppRoutes("mocktest")
    object VisionBoard : AppRoutes("visionboard")
    object Profile     : AppRoutes("profile")
    object StudyHub    : AppRoutes("studyhub")
    object Flashcards  : AppRoutes("flashcards")
    object BrainGame   : AppRoutes("braingame")
    object VideoNotes  : AppRoutes("videonotes")
}
