package cl.duoc.deepsource.data.mock

import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.domain.model.UserRole

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
        User(id = "user-3", name = "Camila Vega", email = "camila.vega@deepsource.cl"),
        User(id = "user-admin", name = "Administrador Demo", email = "admin@deepsource.cl", role = UserRole.ADMIN)
    )

    val projects = listOf(
        Project(
            "project-1", "Proyecto Atlas", "Producto",
            "Deep Source", 10 * 60, 40 * 60,
            iconIndex = 0
        ),
        Project(
            "project-2", "App Boreal", "Mobile",
            "Nébula Labs", 25 * 60, 60 * 60,
            iconIndex = 1
        ),
        Project(
            "project-3", "Portal Nova", "Web",
            "Nova Retail", 50 * 60, 50 * 60,
            iconIndex = 2
        ),
        Project(
            "project-4", "Lumen CRM", "Research",
            "Lumen Co.", 75 * 60, 90 * 60,
            iconIndex = 3
        )
    )

    // Registros de demostración que explican exactamente los minutos iniciales.
    // No representan fichajes reales de usuarios; son datos de ejemplo.
    val timeEntries = listOf(
        TimeEntry("entry-1", "project-1", currentUser.id, 150, "2026-10-03", "Configuración inicial (demo)"),
        TimeEntry("demo-atlas-restante", "project-1", currentUser.id, 450, "2026-10-03", "Horas iniciales de ejemplo"),
        TimeEntry("demo-boreal", "project-2", currentUser.id, 1500, "2026-10-03", "Horas iniciales de ejemplo"),
        TimeEntry("demo-nova", "project-3", currentUser.id, 3000, "2026-10-03", "Horas iniciales de ejemplo"),
        TimeEntry("demo-lumen", "project-4", currentUser.id, 4500, "2026-10-03", "Horas iniciales de ejemplo")
    )
}
