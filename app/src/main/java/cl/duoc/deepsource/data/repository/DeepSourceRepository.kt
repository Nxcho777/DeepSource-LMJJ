package cl.duoc.deepsource.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.local.toDomain
import cl.duoc.deepsource.data.local.toEntity
import cl.duoc.deepsource.data.mock.MockData
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

    suspend fun getProjects(): List<Project> {
        seedIfNeeded()
        return dao.getProjects().map { it.toDomain() }
    }

    suspend fun getUser(userId: String): User? {
        seedIfNeeded()
        return dao.getUser(userId)?.toDomain()
    }

    suspend fun addTimeEntry(projectId: String, userId: String, durationMinutes: Int, date: String, note: String): TimeEntry {
        require(durationMinutes > 0) { "La duración debe ser mayor que cero." }
        require(date.isNotBlank()) { "La fecha es obligatoria." }
        seedIfNeeded()
        return database.withTransaction {
            val project = requireNotNull(dao.getProject(projectId)) { "Proyecto no encontrado." }
            requireNotNull(dao.getUser(userId)) { "Usuario no encontrado." }
            require(project.registeredMinutes <= Int.MAX_VALUE - durationMinutes) { "El total de minutos excede el límite." }
            val entry = TimeEntry(UUID.randomUUID().toString(), projectId, userId, durationMinutes, date, note)
            dao.insertTimeEntry(entry.toEntity())
            check(dao.addMinutes(projectId, durationMinutes) == 1) { "No se pudieron actualizar los minutos del proyecto." }
            entry
        }
    }

    suspend fun createProject(name: String, client: String, description: String, totalHours: Int, iconIndex: Int): Project {
        val cleanName = name.trim()
        val cleanClient = client.trim()
        val cleanDescription = description.trim()
        require(cleanName.isNotEmpty()) { "Ingresa el nombre del proyecto." }
        require(cleanClient.isNotEmpty()) { "Ingresa el cliente o empresa." }
        require(cleanDescription.isNotEmpty()) { "Ingresa una descripción." }
        require(iconIndex in 0..3) { "Selecciona un ícono válido." }
        require(totalHours in 1..100000) { "Las horas deben estar entre 1 y 100000." }
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
        dao.insertProject(project.toEntity())
        return project
    }

    suspend fun finishProject(projectId: String) {
        seedIfNeeded()
        require(dao.finishProject(projectId) == 1) { "El proyecto ya está terminado o no existe." }
    }

    suspend fun seedIfNeeded() {
        database.withTransaction {
            if (!dao.isSeeded()) {
                if (dao.businessRowCount() == 0) {
                    // These minutes already include the historical seed entry; do not add it again.
                    dao.insertUsers(MockData.users.map { it.toEntity() })
                    dao.insertProjects(MockData.projects.map { it.toEntity() })
                    dao.insertTimeEntries(MockData.timeEntries.map { it.toEntity() })
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
