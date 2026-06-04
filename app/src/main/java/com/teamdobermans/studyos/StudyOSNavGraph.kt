package com.teamdobermans.studyos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teamdobermans.studyos.viewModel.AuthViewModel
import com.teamdobermans.studyos.viewModel.DashboardViewModel
import com.teamdobermans.studyos.viewModel.MockTestViewModel
import com.teamdobermans.studyos.viewModel.NoteViewModel
import com.teamdobermans.studyos.viewModel.PlanViewModel
import com.teamdobermans.studyos.viewModel.PomodoroViewModel
import com.teamdobermans.studyos.viewModel.ProfileViewModel
import com.teamdobermans.studyos.viewModel.ProgressViewModel
import com.teamdobermans.studyos.viewModel.SettingsViewModel
import com.teamdobermans.studyos.viewModel.VisionBoardViewModel

val bottomNavRoutes = setOf(
    AppRoutes.Dashboard.route,
    AppRoutes.Notes.route,
    AppRoutes.Plan.route,
    AppRoutes.Progress.route,
    AppRoutes.Settings.route
)

@Composable
fun StudyOSNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute  = backStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                StudyOSBottomNav(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(innerPadding)
        ) {

            composable(AppRoutes.Auth.route) {
                AuthScreen(
                    onSignUpClick = { navController.navigate(AppRoutes.SignUp.route) },
                    onSignInClick = { navController.navigate(AppRoutes.Login.route) }
                )
            }

            composable(AppRoutes.Login.route) {
                val vm = viewModel<AuthViewModel>()
                LoginBody(
                    viewModel      = vm,
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.Dashboard.route) {
                            popUpTo(AppRoutes.Auth.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.SignUp.route) {
                val vm = viewModel<AuthViewModel>()
                SignUpBody(
                    viewModel       = vm,
                    onSignUpSuccess = {
                        navController.navigate(AppRoutes.Dashboard.route) {
                            popUpTo(AppRoutes.Auth.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Dashboard.route) {
                val vm = viewModel<DashboardViewModel>()
                DashboardBody(
                    viewModel             = vm,
                    onNavigatePomodoro    = { navController.navigate(AppRoutes.Pomodoro.route) },
                    onNavigateVisionBoard = { navController.navigate(AppRoutes.VisionBoard.route) },
                    onNavigateProfile     = { navController.navigate(AppRoutes.Profile.route) }
                )
            }

            composable(AppRoutes.Notes.route) {
                val vm = viewModel<NoteViewModel>()
                NotesScreen(
                    viewModel   = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Plan.route) {
                val vm = viewModel<PlanViewModel>()
                PlanBody(viewModel = vm)
            }

            composable(AppRoutes.Progress.route) {
                val vm = viewModel<ProgressViewModel>()
                ProgressBody(viewModel = vm)
            }

            composable(AppRoutes.Settings.route) {
                val vm = viewModel<SettingsViewModel>()
                SettingsBody(
                    viewModel         = vm,
                    onSignOut         = {
                        navController.navigate(AppRoutes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateProfile = { navController.navigate(AppRoutes.Profile.route) }
                )
            }

            composable(AppRoutes.Pomodoro.route) {
                val vm = viewModel<PomodoroViewModel>()
                PomodoroBody(
                    viewModel = vm,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.MockTest.route) {
                val vm = viewModel<MockTestViewModel>()
                MockTestBody(
                    viewModel = vm,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.VisionBoard.route) {
                val vm = viewModel<VisionBoardViewModel>()
                VisionBoardBody(
                    viewModel = vm,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Profile.route) {
                val vm = viewModel<ProfileViewModel>()
                ProfileBody(
                    viewModel = vm,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.StudyHub.route) {
                StudyUI(
                    onNavigateMockTest   = { navController.navigate(AppRoutes.MockTest.route) },
                    onNavigateFlashcard  = { navController.navigate(AppRoutes.Flashcards.route) },
                    onNavigateBrainGame  = { navController.navigate(AppRoutes.BrainGame.route) },
                    onNavigateVideoNotes = { navController.navigate(AppRoutes.VideoNotes.route) }
                )
            }

            composable(AppRoutes.Flashcards.route) {
                FlashcardsScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.BrainGame.route) {
                BrainGameScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.VideoNotes.route) {
                VideoToNotesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
