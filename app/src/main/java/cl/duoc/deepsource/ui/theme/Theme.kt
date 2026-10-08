package cl.duoc.deepsource.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun DeepSourceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = DeepSourceTeal,
            onPrimary = DeepSourceInk,
            background = DeepSourceInk,
            onBackground = DeepSourceWhite,
            surface = DeepSourceSurface,
            onSurface = DeepSourceWhite
        ),
        content = content
    )
}
