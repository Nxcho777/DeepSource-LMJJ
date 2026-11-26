package cl.duoc.deepsource.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.local.toDomain
import cl.duoc.deepsource.data.local.toEntity
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.domain.model.ProjectTask
import cl.duoc.deepsource.data.local.ProjectTaskEntity
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DeepSourceRepository(private val database: DeepSourceDatabase) {
    private val dao = database.dao()

    fun observeProjects(): Flow<List<Project>> = flow {
        seedIfNeeded()
        emitAll(dao.observeProjects().map { rows -> rows.map { it.toDomain() } })
    }

    fun observeUsers(): Flow<List<User>> = flow {
        seedIfNeeded()
        emitAll(dao.observeUsers().map { rows -> rows.map { it.toDomain() } })
    }

    fun observeTimeEntries(): Flow<List<TimeEntry>> = flow {
        seedIfNeeded()
        emitAll(dao.observeTimeEntries().map { rows -> rows.map { it.toDomain() } })
    }

    fun observeTasks(): Flow<List<ProjectTask>> = flow {
        seedIfNeeded()
        emitAll(dao.observeTasks().map { rows -> rows.map { it.toDomain() } })
    }

    suspend fun getTasks(projectId: String): List<ProjectTask> {
        seedIfNeeded()
        return dao.getTasks(projectId).map { it.toDomain() }
    }

    suspend fun createTask(actorUserId: String, projectId: String, name: String, description: String, estimatedHours: Int): ProjectTask {
        requireAdmin(actorUserId)
        require(name.trim().isNotEmpty()) { "Ingresa el nombre de la tarea." }
        require(estimatedHours in 1..100000) { "Las horas estimadas deben ser mayores que cero." }
        seedIfNeeded()
        val task = ProjectTask(UUID.randomUUID().toString(), projectId, name.trim(), description.trim(), estimatedHours * 60)
        database.withTransaction {
            requireAdmin(actorUserId)
            requireNotNull(dao.getProject(projectId)) { "Proyecto no encontrado." }
            dao.insertTask(task.toEntity())
        }
        return task
    }

    suspend fun getProjects(): List<Project> {
        seedIfNeeded()
        return dao.getProjects().map { it.toDomain() }
    }

    suspend fun getUser(userId: String): User? {
        seedIfNeeded()
        return dao.getUser(userId)?.toDomain()
    }

    suspend fun renameUser(userId: String, name: String) {
        val cleanName = name.trim()
        require(cleanName.length in 1..100) { "Ingresa un nombre de entre 1 y 100 caracteres." }
        seedIfNeeded()
        check(dao.renameUser(userId, cleanName) == 1) { "No se encontró el usuario." }
    }

    suspend fun addTimeEntry(projectId: String, userId: String, durationMinutes: Int, date: String, note: String, taskId: String? = null): TimeEntry {
        require(durationMinutes > 0) { "La duración debe ser mayor que cero." }
        require(date.isNotBlank()) { "La fecha es obligatoria." }
        seedIfNeeded()
        return database.withTransaction {
            val project = requireNotNull(dao.getProject(projectId)) { "Proyecto no encontrado." }
            requireNotNull(dao.getUser(userId)) { "Usuario no encontrado." }
            require(project.registeredMinutes <= Int.MAX_VALUE - durationMinutes) { "El total de minutos excede el límite." }
            val selectedTask = taskId?.let { requireNotNull(dao.getTask(it)) { "Tarea no encontrada." } }
            require(selectedTask == null || selectedTask.projectId == projectId) { "La tarea no pertenece al proyecto." }
            val entry = TimeEntry(UUID.randomUUID().toString(), projectId, userId, durationMinutes, date, note, selectedTask?.id)
            dao.insertTimeEntry(entry.toEntity())
            check(dao.addMinutes(projectId, durationMinutes) == 1) { "No se pudieron actualizar los minutos del proyecto." }
            entry
        }
    }

    suspend fun createProject(actorUserId: String, name: String, client: String, description: String, totalHours: Int, iconIndex: Int,
        initialTasks: List<NewProjectTask> = emptyList()): Project {
        requireAdmin(actorUserId)
        val cleanName = name.trim()
        val cleanClient = client.trim()
        val cleanDescription = description.trim()
        require(cleanName.isNotEmpty()) { "Ingresa el nombre del proyecto." }
        require(cleanClient.isNotEmpty()) { "Ingresa el cliente o empresa." }
        require(cleanDescription.isNotEmpty()) { "Ingresa una descripción." }
        require(iconIndex in 0..3) { "Selecciona un ícono válido." }
        require(totalHours in 1..100000) { "Las horas deben estar entre 1 y 100000." }
        require(initialTasks.all { it.name.isNotBlank() && it.estimatedHours in 1..100000 }) { "Revisa las tareas del proyecto." }
        require(initialTasks.sumOf { it.estimatedHours.toLong() } <= totalHours.toLong()) { "Las horas de las tareas superan las horas del proyecto." }
        seedIfNeeded()
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            description = cleanDescription,
            client = cleanClient,
            registeredMinutes = 0,
            totalMinutes = totalHours * 60,
            iconIndex = iconIndex
        )
        database.withTransaction {
            requireAdmin(actorUserId)
            dao.insertProject(project.toEntity())
            dao.insertTask(ProjectTaskEntity("general-" + project.id, project.id, "Trabajo general", "Tarea inicial del proyecto", project.totalMinutes))
            initialTasks.forEach { task ->
                dao.insertTask(ProjectTaskEntity(UUID.randomUUID().toString(), project.id,
                    task.name.trim(), task.description.trim(), task.estimatedHours * 60))
            }
        }
        return project
    }

    suspend fun finishProject(actorUserId: String, projectId: String) {
        requireAdmin(actorUserId)
        seedIfNeeded()
        database.withTransaction {
            requireAdmin(actorUserId)
            require(dao.finishProject(projectId) == 1) { "El proyecto no existe, ya está terminado o no alcanza el 100 %." }
        }
    }

    private suspend fun requireAdmin(actorUserId: String) {
        seedIfNeeded()
        check(dao.getUser(actorUserId)?.role == "ADMIN") { "No tienes permisos de administrador." }
    }

    suspend fun seedIfNeeded() {
        database.withTransaction {
            if (!dao.isSeeded()) {
                if (dao.businessRowCount() == 0) {
                    // These minutes already include the historical seed entry; do not add it again.
                    dao.insertUsers(MockData.users.map { it.toEntity() })
                    dao.insertProjects(MockData.projects.map { it.toEntity() })
                    dao.insertTimeEntries(MockData.timeEntries.map { it.toEntity() })
                    MockData.projects.forEach { project ->
                        dao.insertTask(ProjectTaskEntity("general-" + project.id, project.id, "Trabajo general", "Tarea inicial del proyecto", project.totalMinutes))
                    }
                    dao.insertTask(ProjectTaskEntity("demo-task-project-1-1", "project-1", "Diseño de interfaz", "Diseñar pantallas y componentes de Atlas", 600))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-1-2", "project-1", "Desarrollo de funcionalidades", "Implementar módulos de Atlas", 1200))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-1-3", "project-1", "Pruebas y documentación", "Validar funcionalidades y documentar", 600))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-2-1", "project-2", "Diseño móvil", "Diseñar la experiencia de App Boreal", 900))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-2-2", "project-2", "Desarrollo Android", "Implementar pantallas y lógica móvil", 1800))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-2-3", "project-2", "Pruebas de aplicación", "Validación y correcciones", 900))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-3-1", "project-3", "Diseño del portal", "Diseñar vistas de Portal Nova", 600))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-3-2", "project-3", "Desarrollo web", "Implementar funcionalidades del portal", 1800))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-3-3", "project-3", "Integración y pruebas", "Integraciones y validación", 600))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-4-1", "project-4", "Modelado CRM", "Definir entidades y procesos del CRM", 1200))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-4-2", "project-4", "Implementación CRM", "Construir módulos y flujos", 2400))
                    dao.insertTask(ProjectTaskEntity("demo-task-project-4-3", "project-4", "Pruebas y entrega", "Verificar y documentar la solución", 1800))
                }
                dao.markSeeded(SeedStateEntity())
            }
        }
    }

    companion object {
        const val DEFAULT_USER_ID = "user-1"

        fun getInstance(context: Context): DeepSourceRepository =
            DeepSourceRepository(DeepSourceDatabase.getInstance(context))
    }
}

/** Tarea que el administrador asigna al crear un proyecto. */
data class NewProjectTask(val name: String, val description: String, val estimatedHours: Int)
