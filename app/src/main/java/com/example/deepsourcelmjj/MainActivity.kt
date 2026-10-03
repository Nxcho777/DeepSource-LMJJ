package com.example.deepsourcelmjj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavHost()
                }
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val rootNavController = rememberNavController()
    var currentUser by remember { mutableStateOf<AppUser?>(null) }

    NavHost(
        navController = rootNavController,
        startDestination = "sign_in"
    ) {
        composable("sign_in") {
            SignInScreen(
                onLoginSuccess = { authenticatedUser ->
                    currentUser = authenticatedUser
                    rootNavController.navigate("main") {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            currentUser?.let { user ->
                MainScreen(
                    currentUser = user,
                    onLogout = {
                        currentUser = null
                        rootNavController.navigate("sign_in") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
