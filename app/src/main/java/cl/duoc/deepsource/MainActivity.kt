package cl.duoc.deepsource

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.navigation.AppDestination
import cl.duoc.deepsource.ui.DeepSourceApp
import cl.duoc.deepsource.ui.theme.DeepSourceTheme

class MainActivity : ComponentActivity() {
    private var startDestination by mutableStateOf(AppDestination.Login.route)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializa la pantalla de carga nativa con fondo oscuro
        installSplashScreen()

        super.onCreate(savedInstanceState)
        handleNavigationIntent(intent)
        setContent {
            DeepSourceTheme {
                key(startDestination) {
                    DeepSourceApp(startDestination = startDestination)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        startDestination = intent.getStringExtra(EXTRA_DESTINATION) ?: AppDestination.Login.route
        intent.getStringExtra(EXTRA_PROJECT_ID)
            ?.takeIf { id -> MockData.projects.any { it.id == id } }
            ?.let { MockData.selectedProjectId.value = it }
    }

    companion object {
        const val EXTRA_DESTINATION = "deepsource_destination"
        const val EXTRA_PROJECT_ID = "deepsource_project_id"
    }
}
