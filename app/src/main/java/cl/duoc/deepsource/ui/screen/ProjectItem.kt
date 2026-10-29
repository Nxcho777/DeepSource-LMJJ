
package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.graphics.Color

data class ProjectItem(
    val id: String,
    val name: String,
    val clientAndType: String,
    val iconRes: Int,
    val hoursRegistered: Int,
    val hoursTotal: Int,
    val isSelected: Boolean = false
) {
    val percentage: Float
        get() = if (hoursTotal > 0) {
            (hoursRegistered.toFloat() / hoursTotal.toFloat()) * 100f
        } else {
            0f
        }

    val isCompleted: Boolean
        get() = hoursRegistered >= hoursTotal

    val progressColor: Color
        get() = when {
            isCompleted -> ProjectsTheme.AccentBlue
            percentage >= 90f -> ProjectsTheme.AccentRed
            percentage >= 66f -> ProjectsTheme.AccentYellow
            else -> ProjectsTheme.AccentTeal
        }
}
