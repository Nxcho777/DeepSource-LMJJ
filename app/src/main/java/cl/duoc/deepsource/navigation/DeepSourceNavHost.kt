package cl.duoc.deepsource.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.ProjectsScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen
import cl.duoc.deepsource.ui.viewmodel.LoginViewModel
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import cl.duoc.deepsource.ui.screen.SplashScreen

private const val TRANSITION_DURATION = 367
private const val EXIT_DURATION = 267
private const val ENTER_DELAY = 45

private fun tabIndex(route: String?): Int = when (route) {
    AppDestination.Projects.route -> 0
    AppDestination.RegisterTime.route -> 1
    AppDestination.Profile.route -> 2
    else -> -1
}

private fun directionalEnter(direction: Int, distancePx: Int): EnterTransition =
    fadeIn(
        initialAlpha = 0f,
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            delayMillis = ENTER_DELAY,
            easing = LinearOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { direction * distancePx },
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    )

private fun directionalExit(direction: Int, distancePx: Int): ExitTransition =
    fadeOut(
        targetAlpha = 0f,
        animationSpec = tween(EXIT_DURATION, easing = FastOutSlowInEasing)
    ) + slideOutHorizontally(
        targetOffsetX = { -direction * distancePx },
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    )

@Composable
fun DeepSourceNavHost(
    navController: NavHostController,
    startDestination: String = AppDestination.Splash.route,
    workspaceViewModel: WorkspaceViewModel
) {
    val density = LocalDensity.current
    val distancePx = with(density) { 36.dp.roundToPx() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: startDestination
    val activeTab = when (currentRoute) {
        AppDestination.Projects.route -> DeepSourceTab.Projects
        AppDestination.RegisterTime.route -> DeepSourceTab.Register
        AppDestination.Profile.route -> DeepSourceTab.Profile
        else -> null
    }

    // La barra queda FUERA del NavHost animado y nunca participa del fade.
    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF071326),
                            Color(0xFF0B2035),
                            Color(0xFF102F36)
                        )
                    )
                ),
            enterTransition = {
                val from = tabIndex(initialState.destination.route)
                val to = tabIndex(targetState.destination.route)
                if (from == -1 || to == -1 || from == to) EnterTransition.None
                else directionalEnter(if (to > from) 1 else -1, distancePx)
            },
            exitTransition = {
                val from = tabIndex(initialState.destination.route)
                val to = tabIndex(targetState.destination.route)
                if (from == -1 || to == -1 || from == to) ExitTransition.None
                else directionalExit(if (to > from) 1 else -1, distancePx)
            },
            popEnterTransition = {
                val from = tabIndex(initialState.destination.route)
                val to = tabIndex(targetState.destination.route)
                if (from == -1 || to == -1 || from == to) EnterTransition.None
                else directionalEnter(if (to > from) 1 else -1, distancePx)
            },
            popExitTransition = {
                val from = tabIndex(initialState.destination.route)
                val to = tabIndex(targetState.destination.route)
                if (from == -1 || to == -1 || from == to) ExitTransition.None
                else directionalExit(if (to > from) 1 else -1, distancePx)
            }
        ) {
            composable(AppDestination.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(
                            AppDestination.Login.route
                        ) {
                            popUpTo(AppDestination.Splash.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(AppDestination.Login.route) {
                val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
                LoginScreen(loginViewModel, onLogin = {
                    navController.navigate(AppDestination.Projects.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
            composable(AppDestination.Projects.route) {
                ProjectsScreen(
                    workspaceViewModel,
                    onRegisterTime = { navController.navigateTab(AppDestination.RegisterTime.route) },
                    onProfile = { navController.navigateTab(AppDestination.Profile.route) }
                )
            }
            composable(AppDestination.RegisterTime.route) {
                RegisterTimeScreen(
                    workspaceViewModel,
                    onProjects = { navController.navigateTab(AppDestination.Projects.route) },
                    onProfile = { navController.navigateTab(AppDestination.Profile.route) }
                )
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    workspaceViewModel,
                    onProjects = { navController.navigateTab(AppDestination.Projects.route) },
                    onRegisterTime = { navController.navigateTab(AppDestination.RegisterTime.route) }
                )
            }
        }

        if (activeTab != null) {
            DeepSourceBottomNavigation(
                activeTab = activeTab,
                onProjects = { navController.navigateTab(AppDestination.Projects.route) },
                onRegister = { navController.navigateTab(AppDestination.RegisterTime.route) },
                onProfile = { navController.navigateTab(AppDestination.Profile.route) }
            )
        }
    }
}

fun NavHostController.navigateTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
