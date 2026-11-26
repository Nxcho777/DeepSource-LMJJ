package cl.duoc.deepsource.domain.model

data class ProjectTask(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val estimatedMinutes: Int,
    val archived: Boolean = false
)
