package com.example.deepsourcelmjj

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object AppRoutes {
    const val LOGIN = "login"
    const val MAIN = "main"
}

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    NavHost(
        navController = rootNavController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            SignInScreen(
                onLoginSuccess = { authenticatedUser ->
                    currentUser = authenticatedUser
                    rootNavController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.MAIN) {
            currentUser?.let { user ->
                MainScreen(
                    currentUser = user,
                    onLogout = {
                        currentUser = null
                        rootNavController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
