package cl.duoc.deepsource.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import cl.duoc.deepsource.data.auth.AuthManager
import cl.duoc.deepsource.ui.screen.LoginScreen
import cl.duoc.deepsource.ui.screen.ProjectsHomeScreen
import cl.duoc.deepsource.ui.screen.RegisterTimeScreen
import cl.duoc.deepsource.ui.screen.ProfileScreen
import cl.duoc.deepsource.ui.screen.SplashScreen
import cl.duoc.deepsource.ui.screen.CreateProjectScreen


@Composable
fun DeepSourceNavHost(
    navController: NavHostController,
    startDestination: String = AppDestination.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route // O el identificador de tu Splash
    ) {
        composable(AppDestination.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(AppDestination.Login.route) { // O tu pantalla siguiente
                        // Elimina el Splash de la pila para que no se pueda regresar a él
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla de Inicio de Sesión
        composable(AppDestination.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    val isSuccess = AuthManager.login(email, password)
                    if (isSuccess) {
                        navController.navigate(AppDestination.Projects.route) {
                            popUpTo(AppDestination.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                    isSuccess
                }
            )
        }

        // 3. Pantalla Principal de Proyectos
        composable(AppDestination.Projects.route) {
            ProjectsHomeScreen(
                onRegisterClick = { projectId ->
                    navController.navigate(
                        "${AppDestination.RegisterTime.route}/$projectId"
                    )
                },
                onProfileClick = {
                    navController.navigate(AppDestination.Profile.route)
                },
                onCreateProjectClick = {
                    navController.navigate(AppDestination.CreateProject.route)
                }
            )
        }

        composable(AppDestination.CreateProject.route) {
            CreateProjectScreen(
                onBack = {
                    navController.popBackStack()
                },
                onProjectCreated = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Pantalla de Registro de Tiempo (con parámetro de Proyecto)
        composable("${AppDestination.RegisterTime.route}/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments
                ?.getString("projectId") ?: "project-1"

            RegisterTimeScreen(
                projectId = projectId,
                onProjects = {
                    navController.navigate(AppDestination.Projects.route) {
                        popUpTo(AppDestination.Projects.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onProfile = {
                    navController.navigate(AppDestination.Profile.route)
                }
            )
        }

        // 5. Pantalla de Perfil de Usuario
        composable(AppDestination.Profile.route) {
            ProfileScreen(
                onProjectsClick = {
                    navController.navigate(AppDestination.Projects.route) {
                        popUpTo(AppDestination.Projects.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("${AppDestination.RegisterTime.route}/project-1")
                }
            )
        }
    }
}