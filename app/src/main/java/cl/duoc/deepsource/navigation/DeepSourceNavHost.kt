package cl.duoc.deepsource.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.ProjectsScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen
import cl.duoc.deepsource.ui.viewmodel.LoginViewModel
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel

@Composable
fun DeepSourceNavHost(navController: NavHostController, startDestination: String = AppDestination.Login.route, workspaceViewModel: WorkspaceViewModel) {
    NavHost(navController = navController, startDestination = startDestination) {
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
            ProjectsScreen(workspaceViewModel,
                onRegisterTime = { navController.navigateTab(AppDestination.RegisterTime.route) },
                onProfile = { navController.navigateTab(AppDestination.Profile.route) }
            )
        }
        composable(AppDestination.RegisterTime.route) {
            RegisterTimeScreen(workspaceViewModel,
                onProjects = { navController.navigateTab(AppDestination.Projects.route) },
                onProfile = { navController.navigateTab(AppDestination.Profile.route) }
            )
        }
        composable(AppDestination.Profile.route) {
            ProfileScreen(workspaceViewModel,
                onProjects = { navController.navigateTab(AppDestination.Projects.route) },
                onRegisterTime = { navController.navigateTab(AppDestination.RegisterTime.route) }
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
