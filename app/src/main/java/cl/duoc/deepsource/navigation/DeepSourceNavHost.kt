
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.deepsource.ui.screen.SplashScreen

import cl.duoc.deepsource.ui.components.DeepSourceAnimatedBackground
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.ProjectsScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen
import cl.duoc.deepsource.ui.viewmodel.LoginViewModel
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import cl.duoc.deepsource.ui.WidgetNavigationRequest

private const val TRANSITION_DURATION = 367
private const val EXIT_DURATION = 267
private const val ENTER_DELAY = 45

private fun tabIndex(route: String?): Int = when (route) {
    AppDestination.Projects.route -> 0
    AppDestination.RegisterTime.route -> 1
    AppDestination.Profile.route -> 2
    else -> -1
}

private fun directionalEnter(
    direction: Int,
    distancePx: Int
): EnterTransition {
    return fadeIn(
        initialAlpha = 0f,
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            delayMillis = ENTER_DELAY,
            easing = LinearOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { direction * distancePx },
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

private fun directionalExit(
    direction: Int,
    distancePx: Int
): ExitTransition {
    return fadeOut(
        targetAlpha = 0f,
        animationSpec = tween(
            durationMillis = EXIT_DURATION,
            easing = FastOutSlowInEasing
        )
    ) + slideOutHorizontally(
        targetOffsetX = { -direction * distancePx },
        animationSpec = tween(
            durationMillis = TRANSITION_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

private const val MAIN_ROUTE = "deepsource_main"

@Composable
fun DeepSourceNavHost(
    navController: NavHostController,
    startDestination: String = AppDestination.Splash.route,
    workspaceViewModel: WorkspaceViewModel,
    widgetRequest: WidgetNavigationRequest? = null
) {
    // El NavHost exterior solo cambia entre Splash, Login y la aplicación autenticada.
    // La barra se crea DENTRO de MAIN_ROUTE, nunca sobre Login.
    val openFromWidget = widgetRequest != null ||
            startDestination == AppDestination.RegisterTime.route

    val initialRoute = when {
        openFromWidget -> MAIN_ROUTE
        startDestination == AppDestination.Projects.route ||
                startDestination == AppDestination.Profile.route -> MAIN_ROUTE
        else -> startDestination
    }

    Box(Modifier.fillMaxSize()) {
        DeepSourceAnimatedBackground(modifier = Modifier.fillMaxSize())

        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(AppDestination.Splash.route) {
                SplashScreen(onSplashFinished = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }

            composable(AppDestination.Login.route) {
                val loginViewModel: LoginViewModel =
                    viewModel(factory = LoginViewModel.Factory)
                LoginScreen(loginViewModel, onLogin = {
                    workspaceViewModel.setCurrentUserByEmail(loginViewModel.loginEmail())
                    navController.navigate(MAIN_ROUTE) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }

            composable(MAIN_ROUTE) {
                DeepSourceAuthenticatedContent(
                    workspaceViewModel = workspaceViewModel,
                    initialTab = when (startDestination) {
                        AppDestination.RegisterTime.route -> AppDestination.RegisterTime.route
                        AppDestination.Profile.route -> AppDestination.Profile.route
                        else -> if (openFromWidget) AppDestination.RegisterTime.route
                        else AppDestination.Projects.route
                    },
                    widgetRequest = widgetRequest,
                    onLogout = {
                        workspaceViewModel.logout()
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(MAIN_ROUTE) { inclusive = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DeepSourceAuthenticatedContent(
    workspaceViewModel: WorkspaceViewModel,
    initialTab: String,
    widgetRequest: WidgetNavigationRequest?,
    onLogout: () -> Unit
) {
    val tabsController = rememberNavController()
    val density = LocalDensity.current
    val distancePx = with(density) { 36.dp.roundToPx() }
    val backStackEntry by tabsController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: initialTab

    val activeTab = when (currentRoute) {
        AppDestination.Projects.route -> DeepSourceTab.Projects
        AppDestination.RegisterTime.route -> DeepSourceTab.Register
        AppDestination.Profile.route -> DeepSourceTab.Profile
        else -> DeepSourceTab.Projects
    }

    LaunchedEffect(widgetRequest?.sequence) {
        widgetRequest?.let { request ->
            val target = if (request.destination == AppDestination.Profile.route)
                AppDestination.Profile.route else AppDestination.RegisterTime.route
            if (target == AppDestination.RegisterTime.route) {
                request.projectId?.let(workspaceViewModel::selectProject)
            }
            if (tabsController.currentDestination?.route != target) {
                tabsController.navigateTab(target)
            }
        }
    }

    // La pantalla y Liquid Glass nacen en la misma composición.
    // Solo las pantallas interiores se animan entre pestañas.
    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = tabsController,
            startDestination = initialTab,
            modifier = Modifier.fillMaxSize(),
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
            composable(AppDestination.Projects.route) {
                ProjectsScreen(
                    workspaceViewModel,
                    onRegisterTime = {
                        tabsController.navigateTab(AppDestination.RegisterTime.route)
                    },
                    onProfile = {
                        tabsController.navigateTab(AppDestination.Profile.route)
                    }
                )
            }

            composable(AppDestination.RegisterTime.route) {
                RegisterTimeScreen(
                    workspaceViewModel,
                    onProjects = {
                        tabsController.navigateTab(AppDestination.Projects.route)
                    },
                    onProfile = {
                        tabsController.navigateTab(AppDestination.Profile.route)
                    }
                )
            }

            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    workspaceViewModel,
                    onProjects = {
                        tabsController.navigateTab(AppDestination.Projects.route)
                    },
                    onRegisterTime = {
                        tabsController.navigateTab(AppDestination.RegisterTime.route)
                    },
                    onLogout = onLogout
                )
            }
        }

        DeepSourceBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            activeTab = activeTab,
            onProjects = {
                tabsController.navigateTab(AppDestination.Projects.route)
            },
            onRegister = {
                tabsController.navigateTab(AppDestination.RegisterTime.route)
            },
            onProfile = {
                tabsController.navigateTab(AppDestination.Profile.route)
            }
        )
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
