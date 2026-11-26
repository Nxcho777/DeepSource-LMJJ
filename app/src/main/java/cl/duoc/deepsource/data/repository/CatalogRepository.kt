package cl.duoc.deepsource.data.repository

import androidx.room.withTransaction
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.local.toDomain
import cl.duoc.deepsource.data.remote.*
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.util.UUID

class CatalogRepository(
    private val database: DeepSourceDatabase,
    private val projects: ProjectsApi,
    private val tasks: TasksApi
) {
    private val dao = database.dao()
    private val local = DeepSourceRepository(database)
    private val mutex = Mutex()

    suspend fun refresh() = mutex.withLock {
        local.seedIfNeeded()
        val adopt = !dao.isCatalogAdopted()
        if (adopt) adoptCatalog()
        val remoteProjects = projects.list()
        val remoteTasks = tasks.listAll().toMutableList()
        val knownTasks = remoteTasks.mapTo(mutableSetOf()) { it.id }
        for (project in remoteProjects.filterNot { it.archived }) {
            val general = generalTask(project)
            if (general.id !in knownTasks) {
                remoteTasks += createTaskIfMissing(general)
            }
        }
        database.withTransaction {
            remoteProjects.forEach { cache(it) }
            remoteTasks.forEach { if (dao.getProject(it.projectId) != null) dao.upsertTask(it.toEntity()) }
            if (adopt) dao.markSeeded(SeedStateEntity(2))
        }
    }

    private suspend fun adoptCatalog() {
        val activeProjectIds = mutableSetOf<String>()
        for (row in dao.getProjects().filterNot { it.archived }) {
            val dto = row.toDto()
            val remote = onConflict({ projects.create(dto) }, { projects.get(dto.id) })
            if (!remote.archived) activeProjectIds += remote.id
        }
        for (row in dao.getCatalogTasks()) {
            if (!row.archived && row.projectId in activeProjectIds) createTaskIfMissing(row.toDto())
        }
    }

    suspend fun saveProject(
        actor: String, existing: Project?, name: String, client: String, description: String, hours: Int, iconIndex: Int,
        newId: String = UUID.randomUUID().toString()
    ): Project = mutex.withLock {
        requireAdmin(actor)
        require(name.isNotBlank() && client.isNotBlank() && description.isNotBlank()) { "Completa los datos del proyecto." }
        require(hours in 1..100000 && iconIndex in 0..15) { "Revisa las horas y el ícono del proyecto." }
        val dto = ProjectDto(existing?.id ?: newId,
            name.trim(), description.trim(), client.trim(), hours * 60, iconIndex, existing?.isCompleted ?: false)
        val saved = if (existing == null) {
            onConflict({ projects.create(dto) }, {
                projects.get(dto.id).also { check(it == dto) { "El proyecto ya se guardó con otros datos. Actualiza el catálogo." } }
            })
        } else projects.update(dto.id, dto)
        val project = database.withTransaction { cache(saved).toDomain() }
        if (existing == null) {
            try {
                dao.upsertTask(createTaskIfMissing(generalTask(saved)).toEntity())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Refresh retries initialization using the same task ID.
            }
        }
        project
    }

    suspend fun finishProject(actor: String, id: String) = mutex.withLock {
        requireAdmin(actor)
        val localProject = requireNotNull(dao.getProject(id)) { "Proyecto no encontrado." }
        require(!localProject.archived && !localProject.isCompleted &&
            localProject.registeredMinutes >= localProject.totalMinutes) { "El proyecto no alcanza el 100 % o ya está terminado." }
        val remote = projects.get(id)
        require(localProject.registeredMinutes >= remote.totalMinutes) { "El proyecto no alcanza el 100 %." }
        val saved = projects.update(id, remote.copy(isCompleted = true))
        database.withTransaction { cache(saved) }
    }

    suspend fun deleteProject(actor: String, id: String) = mutex.withLock {
        requireAdmin(actor)
        projects.delete(id)
        dao.archiveProject(id)
    }

    suspend fun saveTask(
        actor: String, projectId: String, existing: ProjectTask?, name: String, description: String, hours: Int,
        newId: String = UUID.randomUUID().toString()
    ): ProjectTask = mutex.withLock {
        requireAdmin(actor)
        require(name.isNotBlank() && hours in 1..100000) { "Revisa el nombre y las horas de la tarea." }
        require(dao.getProject(projectId)?.archived == false) { "Proyecto no disponible." }
        val dto = TaskDto(existing?.id ?: newId, projectId, name.trim(), description.trim(), hours * 60)
        val saved = if (existing == null) {
            onConflict({ tasks.create(projectId, dto) }, {
                tasks.get(projectId, dto.id).also { check(it == dto) { "La tarea ya se guardó con otros datos. Actualiza el catálogo." } }
            })
        } else tasks.update(projectId, dto.id, dto)
        saved.toEntity().also { dao.upsertTask(it) }.toDomain()
    }

    suspend fun deleteTask(actor: String, task: ProjectTask) = mutex.withLock {
        requireAdmin(actor)
        tasks.delete(task.projectId, task.id)
        dao.archiveTask(task.id)
    }

    private suspend fun cache(dto: ProjectDto) =
        dto.toEntity(dao.getProject(dto.id)?.registeredMinutes ?: 0).also { dao.upsertProject(it) }

    private suspend fun requireAdmin(actor: String) {
        local.seedIfNeeded()
        check(dao.getUser(actor)?.role == "ADMIN") { "No tienes permisos de administrador." }
    }

    private fun generalTask(project: ProjectDto) =
        TaskDto("general-${project.id}", project.id, "Trabajo general", "Tarea inicial del proyecto", project.totalMinutes)

    private suspend fun createTaskIfMissing(task: TaskDto): TaskDto =
        onConflict({ tasks.create(task.projectId, task) }, { tasks.get(task.projectId, task.id) })

    private suspend fun <T> onConflict(create: suspend () -> T, read: suspend () -> T): T = try {
        create()
    } catch (error: HttpException) {
        if (error.code() == 409) read() else throw error
    }
}
