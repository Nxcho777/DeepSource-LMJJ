package cl.duoc.deepsource.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val client: String,
    val registeredMinutes: Int,
    val totalMinutes: Int
) {
    val status: ProjectStatus
        get() = ProjectStatus.fromMinutes(registeredMinutes, totalMinutes)

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
    Initial("Inicial", 0xFF20EAA7),
    Medium("Medio", 0xFFF4F653),
    NoTime("Sin tiempo", 0xFFE43939),
    Overtime("Sobretiempo", 0xFF313131),
    Finished("Terminado", 0xFF18CBE8);

    companion object {
        fun fromMinutes(registeredMinutes: Int, totalMinutes: Int): ProjectStatus = when {
            registeredMinutes <= 0 -> NoTime
            totalMinutes > 0 && registeredMinutes > totalMinutes -> Overtime
            totalMinutes > 0 && registeredMinutes == totalMinutes -> Finished
            totalMinutes > 0 && registeredMinutes.toFloat() / totalMinutes <= .25f -> Initial
            else -> Medium
        }
    }
}

fun Int.toTimeLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return if (minutes == 0) "$hours h" else "$hours h ${minutes.toString().padStart(2, '0')} min"
}
