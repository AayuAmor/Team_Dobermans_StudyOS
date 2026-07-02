package com.teamdobermans.studyos.ui.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.teamdobermans.studyos.AuthScreen
import com.teamdobermans.studyos.ui.profile.ProgressBody
import com.teamdobermans.studyos.ui.auth.LoginBody
import com.teamdobermans.studyos.ui.auth.SignUpBody
import com.teamdobermans.studyos.ui.focus.BrainGameScreen
import com.teamdobermans.studyos.ui.focus.FocusScreen
import com.teamdobermans.studyos.ui.focus.PomodoroActivity
import com.teamdobermans.studyos.ui.focus.PomodoroBody
import com.teamdobermans.studyos.ui.home.DashboardBody
import com.teamdobermans.studyos.ui.home.VisionBoardBody
import com.teamdobermans.studyos.ui.plan.PlanBody
import com.teamdobermans.studyos.ui.profile.ProfileScreenV2
import com.teamdobermans.studyos.ui.profile.SettingsBody
import com.teamdobermans.studyos.ui.study.FlashcardsScreen
import com.teamdobermans.studyos.ui.study.QuizSetupBody
import com.teamdobermans.studyos.ui.study.NotesScreen
import com.teamdobermans.studyos.ui.study.StudyScreen
import com.teamdobermans.studyos.ui.study.VideoToNotesScreen
import com.teamdobermans.studyos.utils.GoogleSignInHelper
import com.teamdobermans.studyos.viewModel.*

@Composable
fun StudyOSNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                StudyOSBottomNav(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(AppRoutes.Auth.route) {
                AuthScreen(
                    onSignUpClick = { navController.navigate(AppRoutes.SignUp.route) },
                    onSignInClick = { navController.navigate(AppRoutes.Login.route) }
                )
            }

            composable(AppRoutes.Login.route) {
                val vm = viewModel<AuthViewModel>()
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        vm.signInWithCredential(credential)
                    } catch (e: ApiException) {
                        Toast.makeText(context, "Google error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                LoginBody(
                    viewModel = vm,
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.Home.route) {
                            popUpTo(AppRoutes.Auth.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onSignUpClick = { navController.navigate(AppRoutes.SignUp.route) },
                    onGoogleSignIn = { launcher.launch(GoogleSignInHelper.getSignInIntent(context)) }
                )
            }

            composable(AppRoutes.SignUp.route) {
                val vm = viewModel<AuthViewModel>()
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        vm.signInWithCredential(credential)
                    } catch (e: ApiException) {
                        Toast.makeText(context, "Google error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                SignUpBody(
                    viewModel = vm,
                    onSignUpSuccess = {
                        navController.navigate(AppRoutes.Home.route) {
                            popUpTo(AppRoutes.Auth.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onSignInClick = { navController.navigate(AppRoutes.Login.route) },
                    onGoogleSignIn = { launcher.launch(GoogleSignInHelper.getSignInIntent(context)) }
                )
            }

            composable(AppRoutes.Home.route) {
                val vm = viewModel<DashboardViewModel>()
                val ctx = LocalContext.current
                DashboardBody(
                    viewModel = vm,
                    onNavigatePomodoro = {
                        ctx.startActivity(
                            Intent(ctx, PomodoroActivity::class.java).putExtra("auto_start", true)
                        )
                    },
                    onNavigateVisionBoard = {
                        navController.navigate(AppRoutes.VisionBoard.route)
                    },
                    onNavigateProfile = { navController.navigate(AppRoutes.Profile.route) },
                    onNavigateAnalytics = { navController.navigate(AppRoutes.Analytics.route) },
                    onNavigateFlashcards = { navController.navigate(AppRoutes.Flashcards.route) },
                    onNavigateQuiz = { navController.navigate(AppRoutes.MockTest.route) },
                    onNavigateBrainGame = { navController.navigate(AppRoutes.BrainGame.route()) },
                    onNavigateVideoNotes = { navController.navigate(AppRoutes.VideoNotes.route) },
                    onNavigateMockTest = { navController.navigate(AppRoutes.MockTest.route) },
                    onNavigateNotes = { navController.navigate(AppRoutes.Notes.route) }
                )
            }

            composable(AppRoutes.Study.route) {
                val vm = viewModel<StudyViewModel>()
                StudyScreen(
                    viewModel = vm,
                    onNavigateNotes = { navController.navigate(AppRoutes.Notes.route) },
                    onNavigateFlashcards = { navController.navigate(AppRoutes.Flashcards.route) },
                    onNavigateMockTest = { navController.navigate(AppRoutes.MockTest.route) },
                    onNavigateVideoNotes = { navController.navigate(AppRoutes.VideoNotes.route) },
                    onNavigateFocusTimer = { navController.navigate(AppRoutes.Focus.route) }
                )
            }

            composable(AppRoutes.Focus.route) {
                val vm = viewModel<FocusViewModel>()
                FocusScreen(
                    focusViewModel = vm,
                    onNavigateBrainGame = { mode -> navController.navigate(AppRoutes.BrainGame.route(mode)) }
                )
            }

            composable(AppRoutes.Plan.route) {
                val vm = viewModel<PlanViewModel>()
                PlanBody(viewModel = vm)
            }

            composable(AppRoutes.Profile.route) {
                val vm = viewModel<SettingsViewModel>()
                SettingsBody(
                    viewModel = vm,
                    onSignOut = {
                        navController.navigate(AppRoutes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppRoutes.Notes.route) {
                val vm = viewModel<NoteViewModel>()
                NotesScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() },
                    onNavigateVideoNotes = { navController.navigate(AppRoutes.VideoNotes.route) }
                )
            }

            composable(AppRoutes.Analytics.route) {
                val vm = viewModel<AnalyticsViewModel>()
                ProgressBody(
                    analyticsViewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Flashcards.route) {
                val vm = viewModel<FlashcardViewModel>()
                FlashcardsScreen(
                    onBack = { navController.popBackStack() },
                    onCreateNote = { navController.navigate(AppRoutes.Notes.route) },
                    viewModel = vm
                )
            }

            composable("${AppRoutes.BrainGame.route}?sessionId={sessionId}") { entry ->
                val mode = entry.arguments?.getString("mode") ?: AppRoutes.BrainGame.MODE_HUB
                val sessionId = entry.arguments?.getString("sessionId")
                BrainGameScreen(mode = mode, sessionId = sessionId, onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.VideoNotes.route) {
                VideoToNotesScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.MockTest.route) {
                val vm = viewModel<MockTestViewModel>()
                QuizSetupBody(
                    vm = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Pomodoro.route) {
                PomodoroBody(onBack = { navController.popBackStack() })
            }

            composable(AppRoutes.VisionBoard.route) {
                val vm = viewModel<VisionBoardViewModel>()
                VisionBoardBody(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoutes.Settings.route) {
                val vm = viewModel<SettingsViewModel>()
                SettingsBody(
                    viewModel = vm,
                    onSignOut = {
                        navController.navigate(AppRoutes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateProfile = { navController.navigate(AppRoutes.Profile.route) }
                )
            }
        }
    }
}
