package cl.duoc.deepsource.domain.model

data class TimeEntry(
    val id: String,
    val projectId: String,
    val userId: String,
    val durationMinutes: Int,
    val date: String,
    val note: String
)
