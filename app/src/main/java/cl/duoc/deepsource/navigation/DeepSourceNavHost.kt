package cl.duoc.deepsource.navigation

import androidx.compose.runtime.getValue
import cl.duoc.deepsource.data.auth.AuthManager
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.ProjectsScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen

@Composable
fun DeepSourceNavHost(navController: NavHostController, startDestination: String = AppDestination.Login.route) {
    val currentUser by AuthManager.currentUser
    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                onLogin = {
                    if (AuthManager.isAuthenticated()) {
                        navController.navigate(AppDestination.Projects.route) {
                            popUpTo(AppDestination.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(AppDestination.Projects.route) {
            ProjectsScreen(
                onRegisterTime = { navController.navigate(AppDestination.RegisterTime.route) },
                onProfile = { navController.navigate(AppDestination.Profile.route) }
            )
        }
        composable(AppDestination.RegisterTime.route) {
            RegisterTimeScreen(
                onProjects = { navController.navigate(AppDestination.Projects.route) },
                onProfile = { navController.navigate(AppDestination.Profile.route) }
            )
        }
        composable(AppDestination.Profile.route) {
            ProfileScreen(
                onProjects = {
                    navController.navigate(AppDestination.Projects.route)
                },
                onRegisterTime = {
                    navController.navigate(AppDestination.RegisterTime.route)
                },
                onLogout = {
                    AuthManager.logout()

                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}