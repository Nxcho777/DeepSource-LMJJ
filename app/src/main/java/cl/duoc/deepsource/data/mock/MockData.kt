package cl.duoc.deepsource.data.mock

import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User

/** Initial content, read only by the repository when it seeds a new database. */
object MockData {
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

    val projects = listOf(
        Project("project-1", "Proyecto Atlas", "Producto", "Deep Source", 10 * 60, 40 * 60),
        Project("project-2", "App Boreal", "Mobile", "Nébula Labs", 25 * 60, 60 * 60),
        Project("project-3", "Portal Nova", "Web", "Nova Retail", 50 * 60, 50 * 60),
        Project("project-4", "Lumen CRM", "Research", "Lumen Co.", 75 * 60, 90 * 60)
    )

    // Project minutes are existing totals; this entry is historical detail within that total.
    val timeEntries = listOf(
        TimeEntry(id = "entry-1", projectId = "project-1", userId = currentUser.id, durationMinutes = 150, date = "2026-10-03", note = "Configuración inicial")
    )
}
