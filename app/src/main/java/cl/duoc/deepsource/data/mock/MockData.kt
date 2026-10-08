package cl.duoc.deepsource.data.mock

import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object MockData {
    val selectedProjectId = mutableStateOf("project-1")
    val currentUser = User(
        id = "user-1",
        name = "Marina Soto",
        email = "marina.soto@deepsource.cl"
    )

    val users = listOf(
        currentUser,
        User(id = "user-2", name = "Tomás Rojas", email = "tomas.rojas@deepsource.cl"),
        User(id = "user-3", name = "Camila Vega", email = "camila.vega@deepsource.cl")
    )

    val projects = mutableStateListOf(
        Project("project-1", "Proyecto Atlas", "Producto", "Deep Source", 10 * 60, 40 * 60),
        Project("project-2", "App Boreal", "Mobile", "Nébula Labs", 25 * 60, 60 * 60),
        Project("project-3", "Portal Nova", "Web", "Nova Retail", 50 * 60, 50 * 60),
        Project("project-4", "Lumen CRM", "Research", "Lumen Co.", 75 * 60, 90 * 60)
    )

    val timeEntries = mutableStateListOf(
        TimeEntry(id = "entry-1", projectId = "project-1", durationMinutes = 150, date = "2026-10-03", note = "Configuración inicial")
    )

    fun addTimeEntry(projectId: String, durationMinutes: Int, date: String, note: String) {
        require(durationMinutes > 0) { "La duración debe ser mayor que cero." }
        val projectIndex = projects.indexOfFirst { it.id == projectId }
        require(projectIndex >= 0) { "Proyecto no encontrado." }

        val project = projects[projectIndex]
        projects[projectIndex] = project.copy(registeredMinutes = project.registeredMinutes + durationMinutes)
        timeEntries += TimeEntry(
            id = "entry-${timeEntries.size + 1}",
            projectId = projectId,
            durationMinutes = durationMinutes,
            date = date,
            note = note
        )
    }
}
