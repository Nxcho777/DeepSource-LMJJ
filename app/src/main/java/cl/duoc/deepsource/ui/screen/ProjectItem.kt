
package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.graphics.Color

data class ProjectItem(
    val id: String,
    val name: String,
    val clientAndType: String,
    val iconRes: Int,
    val registeredMinutes: Int,
    val totalMinutes: Int,
    val isSelected: Boolean = false
) {
    val percentage: Float
        get() = if (totalMinutes > 0) {
            registeredMinutes.toFloat() /
                    totalMinutes.toFloat() * 100f
        } else {
            0f
        }

    val isCompleted: Boolean
        get() = totalMinutes > 0 &&
                registeredMinutes >= totalMinutes

    val progressColor: Color
        get() = when {
            isCompleted -> ProjectsTheme.AccentBlue
            percentage >= 90f -> ProjectsTheme.AccentRed
            percentage >= 66f -> ProjectsTheme.AccentYellow
            else -> ProjectsTheme.AccentTeal
        }
}
