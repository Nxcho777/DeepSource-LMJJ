package cl.duoc.deepsource.ui.animation

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Compose applies the system duration scale; zero also removes our explicit transitions. */
@Composable
fun motionDurationMillis(normalDurationMillis: Int): Int {
    val resolver = LocalContext.current.contentResolver
    val scale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return if (scale <= 0f) 0 else normalDurationMillis
}
