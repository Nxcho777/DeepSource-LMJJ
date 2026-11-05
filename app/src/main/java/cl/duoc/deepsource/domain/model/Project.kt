package cl.duoc.deepsource.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val client: String,
    val registeredMinutes: Int,
    val totalMinutes: Int,
    val isCompleted: Boolean = false,
    val iconIndex: Int = 0
) {
    val status: ProjectStatus
        get() = if (isCompleted) ProjectStatus.Finished else ProjectStatus.fromMinutes(registeredMinutes, totalMinutes)

    val progress: Float
        get() = if (totalMinutes == 0) 0f else (registeredMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)

    val registeredTimeLabel: String
        get() = registeredMinutes.toTimeLabel()

    val totalTimeLabel: String
        get() = totalMinutes.toTimeLabel()
}

/**
 * Shared visual meaning for a project's time status.
 *
 * The ARGB values intentionally live next to the semantic state so Compose and
 * the traditional RemoteViews widget cannot drift apart.
 */
enum class ProjectStatus(val label: String, val color: Long) {
    Initial("Sin iniciar · 0 %", 0xFF3B82F6),
    Medium("En progreso · 1–49 %", 0xFFFACC15),
    NoTime("Avanzado · 50–100 %", 0xFFEF4444),
    Overtime("Sobretiempo · >100 %", 0xFF242424),
    Finished("Terminado manualmente", 0xFF30B2A6);

    companion object {
        fun fromMinutes(registeredMinutes: Int, totalMinutes: Int): ProjectStatus = when {
            registeredMinutes <= 0 -> Initial
            totalMinutes <= 0 -> Overtime
            registeredMinutes > totalMinutes -> Overtime
            registeredMinutes.toDouble() / totalMinutes < 0.50 -> Medium
            else -> NoTime
        }
    }
}

fun Int.toTimeLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return if (minutes == 0) "$hours h" else "$hours h ${minutes.toString().padStart(2, '0')} min"
}
