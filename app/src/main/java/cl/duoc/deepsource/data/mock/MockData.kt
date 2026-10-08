package cl.duoc.deepsource.data.mock

import cl.duoc.deepsource.domain.model.UserRole
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object MockData {
    val selectedProjectId = mutableStateOf("project-1")

    val users = listOf(
        User(
            id = "user-1",
            name = "Marina Soto",
            email = "marina.soto@deepsource.cl",
            role = UserRole.LEADER
        ),
        User(
            id = "user-2",
            name = "Carlos Lopez",
            email = "carlos.lopez@deepsource.cl",
            role = UserRole.DEV
        ),
        User(
            id = "user-3",
            name = "Juan Perez",
            email = "juan.perez@deepsource.cl",
            role = UserRole.DEV
        )
    )

    val projects = mutableStateListOf(
        Project(
            id = "project-1",
            name = "Proyecto Atlas",
            description = "Producto",
            client = "Deep Source",
            registeredMinutes = 10 * 60,
            totalMinutes = 40 * 60,
            assignedUserIds = listOf("user-1", "user-2", "user-3")
        ),

        Project(
            id = "project-2",
            name = "App Boreal",
            description = "Mobile",
            client = "Nébula Labs",
            registeredMinutes = 25 * 60,
            totalMinutes = 60 * 60,
            assignedUserIds = listOf("user-1", "user-2")
        ),

        Project(
            id = "project-3",
            name = "Portal Nova",
            description = "Web",
            client = "Nova Retail",
            registeredMinutes = 50 * 60,
            totalMinutes = 50 * 60,
            assignedUserIds = listOf("user-1", "user-3")
        ),

        Project(
            id = "project-4",
            name = "Lumen CRM",
            description = "Research",
            client = "Lumen Co.",
            registeredMinutes = 75 * 60,
            totalMinutes = 90 * 60,
            assignedUserIds = listOf("user-1")
        )
    )

    fun getProjectsForUser(userId: String): List<Project> {
        return projects.filter { project ->
            userId in project.assignedUserIds
        }
    }

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
