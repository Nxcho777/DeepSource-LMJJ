package cl.duoc.deepsource

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.duoc.deepsource.navigation.DeepSourceNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                DeepSourceNavHost(navController = navController)
            }
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_PROJECT_ID = "extra_project_id"
    }
}