package cl.duoc.deepsource.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.deepsource.navigation.DeepSourceNavHost

@Composable
fun DeepSourceApp(startDestination: String = "login") {
    DeepSourceNavHost(navController = rememberNavController(), startDestination = startDestination)
}
