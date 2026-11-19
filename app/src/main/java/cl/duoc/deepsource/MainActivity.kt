package cl.duoc.deepsource

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import cl.duoc.deepsource.ui.DeepSourceApp
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.WidgetNavigationRequest
import cl.duoc.deepsource.navigation.AppDestination
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    private var startDestination by mutableStateOf(AppDestination.Splash.route)
    private var widgetRequest by mutableStateOf<WidgetNavigationRequest?>(null)
    private var requestNumber = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startDestination = intent.getStringExtra(EXTRA_DESTINATION) ?: AppDestination.Splash.route
        handleNavigationIntent(intent)
        setContent {
            DeepSourceTheme {
                DeepSourceApp(startDestination = startDestination, widgetRequest = widgetRequest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        val destination = intent.getStringExtra(EXTRA_DESTINATION)
        if (destination == AppDestination.RegisterTime.route || destination == AppDestination.Profile.route) {
            widgetRequest = WidgetNavigationRequest(
                ++requestNumber,
                if (destination == AppDestination.RegisterTime.route) intent.getStringExtra(EXTRA_PROJECT_ID) else null,
                destination!!
            )
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "deepsource_destination"
        const val EXTRA_PROJECT_ID = "deepsource_project_id"
    }
}
