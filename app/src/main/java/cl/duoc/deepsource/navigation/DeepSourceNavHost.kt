package cl.duoc.deepsource.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.ProjectsScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen
import cl.duoc.deepsource.ui.screen.SplashScreen

@Composable
fun DeepSourceNavHost(
    navController: NavHostController,
    startDestination: String = AppDestination.Login.route
) {
    NavHost(navController = navController, startDestination = AppDestination.Splash.route) {
        // Pantalla de Carga
        composable(AppDestination.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(startDestination) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(AppDestination.Login.route) {
            LoginScreen(onLogin = { navController.navigate(AppDestination.Projects.route) })
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
                onProjects = { navController.navigate(AppDestination.Projects.route) },
                onRegisterTime = { navController.navigate(AppDestination.RegisterTime.route) }
            )
        }
    }
}