
package cl.duoc.deepsource.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DeepSourceDarkColorScheme = darkColorScheme(
    primary = DeepSourceTeal,
    onPrimary = Color.White,

    secondary = DeepSourceTealLight,
    onSecondary = DeepSourceBackground,

    tertiary = DeepSourceTealDark,
    onTertiary = Color.White,

    background = DeepSourceBackground,
    onBackground = DeepSourceTextPrimary,

    surface = DeepSourceSurface,
    onSurface = DeepSourceTextPrimary,

    surfaceVariant = DeepSourceBackgroundSecondary,
    onSurfaceVariant = DeepSourceTextSecondary,

    outline = DeepSourceBorder,
    error = DeepSourceError,
    onError = Color.White
)

private val DeepSourceLightColorScheme = lightColorScheme(
    primary = DeepSourceTealDark,
    onPrimary = Color.White,

    secondary = DeepSourceTeal,
    onSecondary = Color.White,

    tertiary = DeepSourceTealDark,
    onTertiary = Color.White,

    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF151B2B),

    surface = Color.White,
    onSurface = Color(0xFF151B2B),

    surfaceVariant = Color(0xFFE8EDF3),
    onSurfaceVariant = Color(0xFF465166),

    outline = Color(0xFFCBD3DF),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun DeepSourceTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DeepSourceDarkColorScheme
    } else {
        DeepSourceLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
