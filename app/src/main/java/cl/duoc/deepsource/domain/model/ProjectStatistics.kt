package cl.duoc.deepsource.domain.model

data class ProjectStatistics(val plannedMinutes: Int, val byUser: List<Pair<String, Long>>) {
    val registeredMinutes: Long = byUser.sumOf { it.second }
    val percent: Int = if (plannedMinutes == 0) 0 else
        ((registeredMinutes.toFloat() / plannedMinutes).coerceIn(0f, 1f) * 100).toInt()
    val pendingMinutes: Long = (plannedMinutes - registeredMinutes).coerceAtLeast(0)
    val hasOvertime: Boolean = plannedMinutes > 0 && registeredMinutes > plannedMinutes

    companion object {
        fun calculate(plannedMinutes: Int, entries: List<TimeEntry>): ProjectStatistics {
            val byUser = entries.groupBy { it.userId }.mapValues { (_, records) ->
                records.sumOf { it.durationMinutes.coerceAtLeast(0).toLong() }
            }.filterValues { it > 0 }.toList().sortedByDescending { it.second }
            return ProjectStatistics(plannedMinutes.coerceAtLeast(0), byUser)
        }
    }
}
