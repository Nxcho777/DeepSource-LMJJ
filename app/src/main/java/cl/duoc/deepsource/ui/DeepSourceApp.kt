package cl.duoc.deepsource.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.deepsource.navigation.DeepSourceNavHost
import cl.duoc.deepsource.ui.theme.DeepSourceTheme

@Composable
fun DeepSourceApp() {
    DeepSourceTheme {
        val navController = rememberNavController()
        DeepSourceNavHost(navController = navController)
    }
}